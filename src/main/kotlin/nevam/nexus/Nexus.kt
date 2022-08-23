package nevam.nexus

import kotlinx.coroutines.flow.Flow
import nevam.Pom
import nevam.nexus.network.RepositoryId

interface Nexus {
  fun stagingRepositories(): List<StagingProfileRepository>

  suspend fun isMetadataPresent(repository: StagingProfileRepository, pom: Pom): Boolean

  fun close(repository: StagingProfileRepository)

  fun pollUntilClosed(repositoryId: RepositoryId): Flow<StatusCheckState>

  fun release(repository: StagingProfileRepository)

  fun pollUntilSyncedToMavenCentral(pom: Pom): Flow<StatusCheckState>

  fun drop(repository: StagingProfileRepository)
}
