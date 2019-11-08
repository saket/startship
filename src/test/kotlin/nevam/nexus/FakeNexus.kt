package nevam.nexus

import io.reactivex.Observable
import nevam.nexus.network.RepositoryId

object FakeNexus : Nexus {
  var repositories = emptyList<StagingProfileRepository>()
  var repository: StagingProfileRepository
    get() = repositories.single()
    set(value) {
      repositories = listOf(value)
    }
  override fun stagingRepositories() = repositories

  override fun close(repository: StagingProfileRepository) = TODO()

  override fun pollUntilClosed(
    repositoryId: RepositoryId
  ): Observable<StatusCheckState> {
    TODO()
  }
}
