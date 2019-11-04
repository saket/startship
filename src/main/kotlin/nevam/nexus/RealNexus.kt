package nevam.nexus

import com.github.ajalt.clikt.core.CliktError

class RealNexus(private val api: NexusApi) : Nexus {

  @Throws(CliktError::class)
  override fun stagingRepositories(): List<StagingProfileRepository> {
    val response = api.stagingRepositories().execute()

    return if (response.isSuccessful) {
      val repositories = response.body()!!.repositories
      repositories
    } else {
      throw CliktError("Failed to connect to nexus.")
    }
  }
}
