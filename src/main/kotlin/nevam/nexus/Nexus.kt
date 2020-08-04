package nevam.nexus

import io.reactivex.Observable
import io.reactivex.Single
import nevam.Pom
import nevam.nexus.network.RepositoryId

interface Nexus {
  fun stagingRepositories(): List<StagingProfileRepository>

  fun isMetadataPresent(repository: StagingProfileRepository, pom: Pom): Single<Boolean>

  fun close(repository: StagingProfileRepository)

  fun pollUntilClosed(repositoryId: RepositoryId): Observable<StatusCheckState>

  fun release(repository: StagingProfileRepository)

  fun pollUntilSyncedToMavenCentral(pom: Pom): Observable<StatusCheckState>

  fun drop(repository: StagingProfileRepository)
}
