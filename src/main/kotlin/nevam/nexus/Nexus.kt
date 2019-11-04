package nevam.nexus

interface Nexus {
  fun stagingRepositories(): List<StagingProfileRepository>
  fun close(repository: StagingProfileRepository)
}
