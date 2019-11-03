package nevam.nexus

object FakeNexusRepository : NexusRepository {
  lateinit var repository: StagingProfileRepository
  override fun stagingRepository() = repository
}
