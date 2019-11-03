package nevam.nexus

object FakeNexus : Nexus {
  lateinit var repository: StagingProfileRepository
  override fun stagingRepository() = repository
}
