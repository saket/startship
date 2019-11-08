package nevam.nexus

import io.reactivex.Observable
import nevam.nexus.network.RepositoryId

interface Nexus {
  fun stagingRepositories(): List<StagingProfileRepository>

  fun close(repository: StagingProfileRepository)

  fun pollUntilClosed(repositoryId: RepositoryId): Observable<StatusCheckState>
}
