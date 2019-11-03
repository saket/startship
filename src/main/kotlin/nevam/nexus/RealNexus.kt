package nevam.nexus

import com.github.ajalt.clikt.core.CliktError

class RealNexus(private val api: NexusApi): Nexus {

  @Throws(CliktError::class)
  override fun stagingRepository(): StagingProfileRepository {
    val response = api.stagingRepositories().execute()

    return if (response.isSuccessful) {
      val repositories = response.body()!!.repositories
      if (repositories.isEmpty()) {
        throw CliktError("You don't have any staged repositories.")
      } else {
        if (repositories.size == 1) {
          repositories.single()
        } else {
          throw CliktError(
              "Did not expect multiple staging repositories. Help the author add support for this? " +
                  "https://github.com/saket/nevam/issues/new. Found repositories: $repositories"
          )
        }
      }
    } else {
      throw CliktError("Failed to connect to nexus.")
    }
  }
}
