package nevam.nexus

import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.Call

class FakeNexusApi : NexusApi {

  override fun stagingRepositories(): Call<StagingRepositoriesResponse> {
    TODO()
  }

  override fun close(profileId: ProfileId, request: CloseStagingRepositoryRequest): Call<Void> {
    TODO()
  }

  var repository = BehaviorSubject.create<StagingProfileRepository>()
  override fun repository(repositoryId: RepositoryId): Single<StagingProfileRepository> {
    return repository.firstOrError()
  }
}
