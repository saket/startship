package nevam.nexus

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.TermUi.echo
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import nevam.Pom
import nevam.util.Observables
import nevam.util.executeAsResult
import nevam.util.mapToResult
import nevam.util.second
import nevam.util.seconds
import nevam.util.stacktraceToString
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Released
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import nevam.nexus.StatusCheckState.Checking
import nevam.nexus.StatusCheckState.Done
import nevam.nexus.StatusCheckState.GaveUp
import nevam.nexus.StatusCheckState.RetryingIn
import nevam.nexus.StatusCheckState.WillRetry
import nevam.nexus.network.ApiResult.Failure
import nevam.nexus.network.ApiResult.Failure.Type
import nevam.nexus.network.ApiResult.Failure.Type.Network
import nevam.nexus.network.ApiResult.Failure.Type.NotFound
import nevam.nexus.network.ApiResult.Failure.Type.Server
import nevam.nexus.network.ApiResult.Failure.Type.UserAuth
import nevam.nexus.network.ApiResult.Success
import nevam.nexus.network.NexusApi
import nevam.nexus.network.RepositoryActionRequest
import nevam.nexus.network.RepositoryId

class RealNexus(
  private val api: NexusApi,
  private val debugMode: Boolean,
  private val config: NexusConfig,
  private val scheduler: Scheduler
) : Nexus {

  @Throws(CliktError::class)
  override fun stagingRepositories(): List<StagingProfileRepository> {
    return when (val result = api.stagingRepositories().executeAsResult()) {
      is Success -> result.response!!.repositories
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun isMetadataPresent(repository: StagingProfileRepository, pom: Pom): Single<Boolean> {
    val repositoryPath = pom.coordinates.mavenDirectory(includeVersion = false)
    return api.stagingMavenMetadata(repository.id, repositoryPath)
        .subscribeOn(scheduler)
        .mapToResult()
        .map {
          when (it) {
            is Success -> true
            is Failure -> when (it.type) {
              NotFound -> false
              UserAuth -> throw invalidCredentialsError()
              else -> throw genericApiError(it)
            }
          }
        }
  }

  override fun close(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repositoryId = repository.id)

    return when (val result = api.close(repository.profileId, request).executeAsResult()) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun pollUntilClosed(repositoryId: RepositoryId): Observable<StatusCheckState> {
    val checkConfig = config.closedStatusCheck

    val giveUpAfterTimer = Observables
        .timer(checkConfig.giveUpAfter, scheduler)
        .map { GaveUp(it) }

    var nextRetryDelaySeconds = checkConfig.initialRetryDelay.seconds
    val increaseDelay = {
      nextRetryDelaySeconds = (nextRetryDelaySeconds * checkConfig.backoffFactor).toLong()
    }

    return api.stagingRepository(repositoryId)
        .subscribeOn(scheduler)
        .mapToResult()
        .map {
          when (it) {
            is Success -> when (val status = it.response!!.status) {
              is Closed -> Done
              is Transitioning -> WillRetry
              is Open -> throw CliktError("Repository is still open! :/")
              is Released -> throw CliktError("Repository is already released!")
              is Unknown -> throw CliktError("Received an unexpected status: ${status.displayValue}")
            }
            is Failure -> when (it.type) {
              Network, Server -> WillRetry
              UserAuth -> throw invalidCredentialsError()
              else -> throw genericApiError(it)
            }
          }
        }
        .toObservable()
        .startWith(Checking)
        .switchMap { status ->
          if (status == WillRetry) {
            Observables.interval(1.second, scheduler = scheduler)
                .map<StatusCheckState> { RetryingIn(nextRetryDelaySeconds - it.seconds) }
                .startWith(WillRetry)
                // Adding +1 to timer because a gap of 5 second means retrying on the 6th second.
                .takeUntil(Observables.timer((nextRetryDelaySeconds + 1).seconds, scheduler))
                .doOnComplete { increaseDelay() }

          } else {
            Observable.just(status)
          }
        }
        .repeat()
        .mergeWith(giveUpAfterTimer)
        .takeUntil { it is Done || it is GaveUp }
  }

  override fun release(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repository.id)

    return when (val result = api.release(repository.profileId, request).executeAsResult()) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun pollUntilSyncedToMavenCentral(pom: Pom): Observable<StatusCheckState> {
    val checkConfig = config.releasedStatusCheck

    val giveUpAfterTimer = Observables
        .timer(checkConfig.giveUpAfter, scheduler)
        .map { GaveUp(it) }

    var nextRetryDelaySeconds = checkConfig.initialRetryDelay.seconds
    val increaseDelay = {
      nextRetryDelaySeconds = (nextRetryDelaySeconds * checkConfig.backoffFactor).toLong()
    }

    return api.releaseMavenMetadata(pom.coordinates.mavenDirectory(includeVersion = false))
        .subscribeOn(scheduler)
        .mapToResult()
        .map {
          when (it) {
            is Success -> {
              when (pom.version == it.response!!.releaseVersion) {
                true -> Done
                else -> WillRetry
              }
            }
            is Failure -> when (it.type) {
              Network, Server, NotFound -> WillRetry
              UserAuth -> throw invalidCredentialsError()
              Type.Unknown -> throw genericApiError(it)
            }
          }
        }
        .toObservable()
        .startWith(Checking)
        // TODO: share code with pollUntilClosed()?
        .switchMap { status ->
          if (status == WillRetry) {
            Observables.interval(1.second, scheduler = scheduler)
                .map<StatusCheckState> { RetryingIn(nextRetryDelaySeconds - it.seconds) }
                .startWith(WillRetry)
                // Adding +1 to timer because a gap of 5 second means retrying on the 6th second.
                .takeUntil(Observables.timer((nextRetryDelaySeconds + 1).seconds, scheduler))
                .doOnComplete { increaseDelay() }

          } else {
            Observable.just(status)
          }
        }
        .repeat()
        .mergeWith(giveUpAfterTimer)
        .takeUntil { it is Done || it is GaveUp }
  }

  override fun drop(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repository.id)

    return when (val result = api.drop(repository.profileId, request).executeAsResult()) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  private fun genericApiError(result: Failure): CliktError {
    if (debugMode && result.error != null) echo(result.error.stacktraceToString())
    return CliktError("Failed to connect to nexus (${result.error})")
  }

  private fun invalidCredentialsError() =
    CliktError("Nexus refused your user credentials. Double-check that your username and password are correct?")
}
