package nevam.nexus

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.TermUi.echo
import com.slack.eithernet.ApiResult
import com.slack.eithernet.ApiResult.Failure
import com.slack.eithernet.ApiResult.Success
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import nevam.Pom
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
import nevam.nexus.network.FailureType.*
import nevam.nexus.network.NexusApi
import nevam.nexus.network.RepositoryActionRequest
import nevam.nexus.network.RepositoryId
import nevam.nexus.network.type
import nevam.util.stacktraceToString
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RealNexus(
  private val api: NexusApi,
  private val debugMode: Boolean,
  private val config: NexusConfig,
  private val ioDispatcher: CoroutineContext,
) : Nexus {

  @Throws(CliktError::class)
  override fun stagingRepositories(): List<StagingProfileRepository> {
    return when (val result = api.stagingRepositories()) {
      is Success -> result.value.repositories
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override suspend fun isMetadataPresent(repository: StagingProfileRepository, pom: Pom): Boolean {
    val repositoryPath = pom.coordinates.mavenDirectory(includeVersion = false)
    return when (val result = api.stagingMavenMetadata(repository.id, repositoryPath)) {
      is Success -> true
      is Failure -> when (result.type) {
        NotFound -> false
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun close(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repositoryId = repository.id)

    return when (val result = api.close(repository.profileId, request)) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun pollUntilClosed(repositoryId: RepositoryId): Flow<StatusCheckState> {
    return retryWithBackoff(config = config.closedStatusCheck) {
      when (val result = api.stagingRepository(repositoryId)) {
        is Success -> when (val status = result.value.status) {
          is Closed -> Done
          is Transitioning -> WillRetry
          is Open -> throw CliktError("Repository is still open! :/")
          is Released -> throw CliktError("Repository is already released!")
          is Unknown -> throw CliktError("Received an unexpected status: ${status.displayValue}")
        }
        is Failure -> when (result.type) {
          Network, Server -> WillRetry
          UserAuth -> throw invalidCredentialsError()
          else -> throw genericApiError(result)
        }
      }
    }.onStart { emit(Checking) }
  }

  override fun release(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repository.id)

    return when (val result = api.release(repository.profileId, request)) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  override fun pollUntilSyncedToMavenCentral(pom: Pom): Flow<StatusCheckState> {
    return retryWithBackoff(config = config.releasedStatusCheck) {
      when (val result = api.releaseMavenMetadata(pom.coordinates.mavenDirectory(includeVersion = false))) {
        is Success -> {
          when (pom.version == result.value.releaseVersion) {
            true -> Done
            else -> WillRetry
          }
        }
        is Failure -> when (result.type) {
          Network, Server, NotFound -> WillRetry
          UserAuth -> throw invalidCredentialsError()
          Unknown -> throw genericApiError(result)
        }
      }
    }.onStart { emit(Checking) }
  }

  override fun drop(repository: StagingProfileRepository) {
    val request = RepositoryActionRequest(repository.id)

    return when (val result = api.drop(repository.profileId, request)) {
      is Success -> Unit
      is Failure -> when (result.type) {
        UserAuth -> throw invalidCredentialsError()
        else -> throw genericApiError(result)
      }
    }
  }

  private fun retryWithBackoff(
    config: NexusConfig.StatusCheck,
    retry: suspend () -> StatusCheckState
  ): Flow<StatusCheckState> {
    return flow {
      var retryDelay = config.initialRetryDelay

      try {
        withTimeout(config.giveUpAfter) {
          while (true) {
            val status = withContext(ioDispatcher) { retry() }
            emit(status)

            if (status is WillRetry) {
              // Wait for a second for the user to read the failure
              // message before overriding it with a loading indicator.
              val delayForUi = 1.seconds
              delay(delayForUi)
              emit(RetryingIn(secondsRemaining = (retryDelay - delayForUi).inWholeSeconds))

              // Adding +1 to timer because to retry on the n+1th second.
              delay(retryDelay + 1.seconds)
              retryDelay *= config.backoffFactor

            } else if (status is Done) {
              break
            }
          }
        }
      } catch (e: TimeoutCancellationException) {
        emit(GaveUp(after = config.giveUpAfter))
      }
    }
  }

  private fun genericApiError(result: ApiResult.Failure<*>): CliktError {
    val throwable = when (result) {
      is Failure.NetworkFailure -> result.error
      is Failure.UnknownFailure -> result.error
      is Failure.HttpFailure -> null
      is Failure.ApiFailure -> null
    }
    if (debugMode && throwable != null) {
      echo(throwable.stacktraceToString())
    }

    val errorMessage = throwable?.message?.let { "($it)" } ?: ""
    return CliktError("Failed to connect to nexus $errorMessage")
  }

  private fun invalidCredentialsError() =
    CliktError("Nexus refused your user credentials. Double-check that your username and password are correct?")
}

private operator fun Duration.times(times: Float): Duration {
  return (inWholeMilliseconds * times.toDouble()).milliseconds
}
