package nevam.nexus.network

import com.slack.eithernet.ApiResult
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

typealias RepositoryId = String // todo: replace with value class
typealias ProfileId = String // todo: replace with value class

interface NexusApi {
  @GET("/service/local/staging/profile_repositories")
  fun stagingRepositories(): ApiResult<StagingRepositoriesResponse, Unit>

  @GET("/service/local/staging/repository/{repositoryId}")
  suspend fun stagingRepository(
    @Path("repositoryId") repositoryId: RepositoryId
  ): ApiResult<StagingProfileRepository, Unit>

  @POST("/service/local/staging/profiles/{profileId}/finish")
  fun close(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): ApiResult<Unit, Unit>

  @POST("/service/local/staging/profiles/{profileId}/promote")
  fun release(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): ApiResult<Unit, Unit>

  @POST("/service/local/staging/profiles/{profileId}/promote")
  fun drop(
    @Path("profileId") profileId: ProfileId,
    @Body request: RepositoryActionRequest
  ): ApiResult<Unit, Unit>

  /** @param repositoryPath e.g., "me/saket/flick". */
  @Headers("Death-To-Xml: true")
  @GET("https://repo1.maven.org/maven2/{repositoryPath}/maven-metadata.xml")
  suspend fun releaseMavenMetadata(
    @Path("repositoryPath", encoded = true) repositoryPath: String
  ): ApiResult<MavenMetadata, Unit>

  /** @param repositoryPath e.g., "me/saket/flick". */
  @Headers("Death-To-Xml: true")
  @GET("/service/local/repositories/{repositoryId}/content/{repositoryPath}/maven-metadata.xml")
  suspend fun stagingMavenMetadata(
    @Path("repositoryId") repositoryId: RepositoryId,
    @Path("repositoryPath", encoded = true) repositoryPath: String
  ): ApiResult<MavenMetadata, Unit>
}
