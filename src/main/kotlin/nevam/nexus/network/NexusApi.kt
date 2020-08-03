package nevam.nexus.network

import io.reactivex.Single
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

typealias RepositoryId = String
typealias ProfileId = String

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): Call<StagingRepositoriesResponse>

  @GET("/service/local/staging/repository/{repositoryId}")
  fun stagingRepository(
    @Path("repositoryId") repositoryId: RepositoryId
  ): Single<StagingProfileRepository>

  @POST("/service/local/staging/profiles/{profileId}/finish")
  fun close(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): Call<Void>

  @POST("/service/local/staging/profiles/{profileId}/promote")
  fun release(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): Call<Void>

  @POST("/service/local/staging/profiles/{profileId}/promote")
  fun drop(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): Call<Void>

  /** @param repositoryPath e.g., "me/saket/flick". */
  @Headers("Death-To-Xml: true")
  @GET("https://repo1.maven.org/maven2/{repositoryPath}/maven-metadata.xml")
  fun releaseMavenMetadata(
    @Path("repositoryPath", encoded = true) repositoryPath: String
  ): Single<MavenMetadata>

  /** @param repositoryPath e.g., "me/saket/flick". */
  @Headers("Death-To-Xml: true")
  @GET("/service/local/repositories/{repositoryId}/content/{repositoryPath}/maven-metadata.xml")
  fun stagingMavenMetadata(
    @Path("repositoryId") repositoryId: RepositoryId,
    @Path("repositoryPath", encoded = true) repositoryPath: String
  ): Single<MavenMetadata>

  // https://oss.sonatype.org/service/local/repositories/releases/content/me/saket/
  //@GET("/service/local/repositories/staging/content/{repositoryPath}/{versionName}")
  //fun stagingRepositoryContent(
  //  @Path("repositoryPath") repositoryPath: String,
  //  @Path("versionName") versionName: String
  //): Call<>

  //@GET("/service/local/staging/repository/{repositoryId}/activity")
  //fun repositoryActivity()
}
