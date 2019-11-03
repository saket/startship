package nevam.nexus

// TODO: rename to Nexus
interface NexusRepository {
  fun stagingRepository(): StagingProfileRepository
}
