package nevam.nexus

import app.cash.turbine.Turbine
import com.slack.eithernet.ApiResult
import nevam.nexus.network.MavenMetadata
import nevam.nexus.network.NexusApi
import nevam.nexus.network.ProfileId
import nevam.nexus.network.RepositoryActionRequest
import nevam.nexus.network.RepositoryId

class FakeNexusApi : NexusApi {
  val repository = Turbine<StagingProfileRepository>()

  override fun stagingRepositories(): ApiResult<StagingRepositoriesResponse, Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun stagingRepository(repositoryId: RepositoryId): ApiResult<StagingProfileRepository, Unit> {
    return ApiResult.success(repository.awaitItem())
  }

  override fun close(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    TODO("Not yet implemented")
  }

  override fun release(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    TODO("Not yet implemented")
  }

  override fun drop(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun releaseMavenMetadata(repositoryPath: String): ApiResult<MavenMetadata, Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun stagingMavenMetadata(
    repositoryId: RepositoryId,
    repositoryPath: String
  ): ApiResult<MavenMetadata, Unit> {
    TODO("Not yet implemented")
  }
}
