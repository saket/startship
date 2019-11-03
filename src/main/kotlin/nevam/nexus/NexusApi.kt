package nevam.nexus

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): Call<StagingRepositoriesResponse>

  //@GET("/service/local/repositories/staging/content/{repositoryPath}/{versionName}")
  //fun stagingRepositoryContent(
  //  @Path("repositoryPath") repositoryPath: String,
  //  @Path("versionName") versionName: String
  //): Call<>

  //@GET("/service/local/staging/repository/{repositoryId}/activity")
  //fun repositoryActivity()
}
