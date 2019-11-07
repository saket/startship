package nevam.nexus

import io.reactivex.Observable
import java.time.Duration

interface Nexus {
  fun stagingRepositories(): List<StagingProfileRepository>

  fun close(repository: StagingProfileRepository)

  fun pollUntilClosed(
    repositoryId: RepositoryId,
    giveUpAfter: Duration
  ): Observable<StatusCheckState>
}
