package nevam.nexus.network

import io.reactivex.Single
import nevam.extensions.hours
import nevam.extensions.minutes
import nevam.extensions.seconds
import nevam.nexus.NexusConfig
import nevam.nexus.NexusConfig.StatusCheck
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit.MILLISECONDS

val MOCK_NEXUS_CONFIG = NexusConfig(
    closedStatusCheck = StatusCheck(
        giveUpAfter = 10.minutes,
        initialRetryDelay = 2.seconds,
        backoffFactor = 1.5f
    ),
    releasedStatusCheck = StatusCheck(
        giveUpAfter = 2.hours,
        initialRetryDelay = 2.seconds,
        backoffFactor = 1.5f
    )
)

class MockNexusApi : NexusApi {

  private class FakeCall<T>(val response: () -> T?) : Call<T> {
    private var executed = false

    override fun enqueue(callback: Callback<T>) {
      TODO()
    }

    override fun isExecuted(): Boolean {
      return executed
    }

    override fun clone(): Call<T> {
      return FakeCall(response)
    }

    override fun isCanceled(): Boolean {
      TODO()
    }

    override fun cancel() {
      TODO()
    }

    override fun execute(): Response<T> {
      executed = true
      return Response.success(response())
    }

    override fun request(): Request {
      TODO()
    }
  }

  override fun stagingRepositories(): Call<StagingRepositoriesResponse> {
    return FakeCall {
      StagingRepositoriesResponse(
          listOf(
//              StagingProfileRepository(
//                  profileName = "me.saket",
//                  id = "mesaket-1042",
//                  type = "closed",
//                  updatedDate = "Wed Nov 06 00:50:51 UTC 2019",
//                  profileId = "999",
//                  isTransitioning = false
//              ),
              StagingProfileRepository(
                  id = "mesaket-1047",
                  type = "open",
                  updatedDate = "Wed Nov 06 01:28:19 UTC 2019",
                  profileId = "999",
                  profileName = "me.saket",
                  isTransitioning = false
              )
          )
      )
    }
  }

  override fun stagingMavenMetadata(repositoryId: RepositoryId, repositoryPath: String): Single<MavenMetadata> {
    TODO()
  }

  override fun close(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    return FakeCall {
      Thread.sleep(500)
      null
    }
  }

  var closedStatusRetryCount = -1
  override fun stagingRepository(repositoryId: RepositoryId): Single<StagingProfileRepository> {
    return Single.fromCallable {
      if (closedStatusRetryCount++ >= 2) {
        StagingProfileRepository(
            id = "mesaket-1042",
            type = "closed",
            updatedDate = "Wed Nov 06 00:50:51 UTC 2019",
            profileId = "999",
            profileName = "me.saket",
            isTransitioning = false
        )
      } else {
        StagingProfileRepository(
            id = "mesaket-1042",
            type = "open",
            updatedDate = "Wed Nov 06 00:50:51 UTC 2019",
            profileId = "999",
            profileName = "me.saket",
            isTransitioning = true
        )
      }
    }.delay(250, MILLISECONDS)
  }

  override fun release(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    return FakeCall {
      Thread.sleep(500)
      null
    }
  }

  override fun drop(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    return FakeCall {
      Thread.sleep(500)
      null
    }
  }

  var releasedStatusRetryCount = -1
  override fun releaseMavenMetadata(repositoryPath: String): Single<MavenMetadata> {
    return Single.fromCallable {
      if (releasedStatusRetryCount++ >= 2) {
        MavenMetadata(
            MavenMetadata.Data(
                groupId = "me.saket",
                artifactId = "nevamtest",
                versions = MavenMetadata.Versions(
                    release = "1.3.1",
                    lastUpdatedDate = "20191108065802"
                )
            )
        )
      } else {
        MavenMetadata(
            MavenMetadata.Data(
                groupId = "me.saket",
                artifactId = "nevamtest",
                versions = MavenMetadata.Versions(
                    release = "1.3.0",
                    lastUpdatedDate = "20191108065802"
                )
            )
        )
      }
    }.delay(250, MILLISECONDS)
  }
}
