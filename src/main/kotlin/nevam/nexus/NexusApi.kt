package nevam.nexus

import retrofit2.Call
import retrofit2.http.GET

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): Call<StagingRepositoriesResponse>

  //@GET("/service/local/staging/repository/{repositoryId}/activity")
  //fun repositoryActivity()
}
