package nevam.nexus.network

import io.reactivex.Single
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

typealias RepositoryId = String
typealias ProfileId = String

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): Call<StagingRepositoriesResponse>

  @POST("/service/local/staging/profiles/{profileId}/finish")
  fun close(
    @Path("profileId") profileId: ProfileId,
    @Body request: CloseStagingRepositoryRequest
  ): Call<Void>

  @GET("/service/local/staging/repository/{repositoryId}")
  fun repository(
    @Path("repositoryId") repositoryId: RepositoryId
  ): Single<StagingProfileRepository>

  //@GET("/service/local/repositories/staging/content/{repositoryPath}/{versionName}")
  //fun stagingRepositoryContent(
  //  @Path("repositoryPath") repositoryPath: String,
  //  @Path("versionName") versionName: String
  //): Call<>

  //@GET("/service/local/staging/repository/{repositoryId}/activity")
  //fun repositoryActivity()
}
