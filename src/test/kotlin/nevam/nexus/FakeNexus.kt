package nevam.nexus

import io.reactivex.Observable
import io.reactivex.Single
import nevam.Pom
import nevam.nexus.network.MavenMetadata
import nevam.nexus.network.RepositoryId

object FakeNexus : Nexus {
  var repositories = emptyList<StagingProfileRepository>()
  var repository: StagingProfileRepository
    get() = repositories.single()
    set(value) {
      repositories = listOf(value)
    }
  override fun stagingRepositories() = repositories

  override fun isMetadataPresent(repository: StagingProfileRepository, pom: Pom): Single<Boolean> {
    TODO()
  }

  override fun close(repository: StagingProfileRepository) = TODO()

  override fun pollUntilClosed(repositoryId: RepositoryId): Observable<StatusCheckState> {
    TODO()
  }

  override fun release(repository: StagingProfileRepository) {
    TODO()
  }

  override fun dropInBackground(repository: StagingProfileRepository) {
    TODO()
  }

  override fun pollUntilSyncedToMavenCentral(pom: Pom): Observable<StatusCheckState> {
    TODO()
  }
}
