package nevam.nexus.network

import io.reactivex.Single
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit.MILLISECONDS

object MockNexusApi : NexusApi {

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

  override fun close(profileId: ProfileId, request: CloseStagingRepositoryRequest): Call<Void> {
    return FakeCall {
      Thread.sleep(500)
      null
    }
  }

  var retryCount = -1

  override fun repository(repositoryId: RepositoryId): Single<StagingProfileRepository> {
    return Single.fromCallable {
      if (retryCount++ >= 2) {
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

  override fun release(profileId: ProfileId, request: ReleaseStagingRepositoryRequest): Call<Void> {
    return FakeCall {
      Thread.sleep(500)
      null
    }
  }
}
