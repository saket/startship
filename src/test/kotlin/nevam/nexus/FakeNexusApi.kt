package nevam.nexus

import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import nevam.nexus.network.MavenMetadata
import nevam.nexus.network.NexusApi
import nevam.nexus.network.ProfileId
import nevam.nexus.network.RepositoryActionRequest
import nevam.nexus.network.RepositoryId
import retrofit2.Call

class FakeNexusApi : NexusApi {
  override fun stagingRepositories(): Call<StagingRepositoriesResponse> {
    TODO()
  }

  var repository = BehaviorSubject.create<StagingProfileRepository>()
  override fun stagingRepository(repositoryId: RepositoryId): Single<StagingProfileRepository> {
    return repository.firstOrError()
  }

  override fun stagingMavenMetadata(repositoryId: RepositoryId, repositoryPath: String): Single<MavenMetadata> {
    TODO()
  }

  override fun close(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    TODO()
  }

  override fun release(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    TODO()
  }

  override fun drop(profileId: ProfileId, request: RepositoryActionRequest): Call<Void> {
    TODO()
  }

  override fun releaseMavenMetadata(repositoryPath: String): Single<MavenMetadata> {
    TODO()
  }
}
