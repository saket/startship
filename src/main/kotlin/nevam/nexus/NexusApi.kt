package nevam.nexus

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): Call<StagingRepositoriesResponse>

  @POST("/service/local/staging/profiles/{profileId}/finish")
  fun close(
    @Path("profileId") profileId: String,
    @Body request: CloseStagingRepositoryRequest
  ): Call<Void>

  //@GET("/service/local/repositories/staging/content/{repositoryPath}/{versionName}")
  //fun stagingRepositoryContent(
  //  @Path("repositoryPath") repositoryPath: String,
  //  @Path("versionName") versionName: String
  //): Call<>

  //@GET("/service/local/staging/repository/{repositoryId}/activity")
  //fun repositoryActivity()
}
