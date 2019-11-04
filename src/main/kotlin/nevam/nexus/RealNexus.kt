package nevam.nexus

import com.github.ajalt.clikt.core.CliktError

class RealNexus(private val api: NexusApi) : Nexus {

  @Throws(CliktError::class)
  override fun stagingRepositories(): List<StagingProfileRepository> {
    val response = api.stagingRepositories().execute()
    return when {
      response.isSuccessful -> response.body()!!.repositories
      response.code() == 401 -> throw CliktError(
          "Nexus refused your user credentials. Check if your username and password are correct?"
      )
      else -> throw CliktError("Failed to connect to nexus.")
    }
  }

  override fun close(repository: StagingProfileRepository) {
    val request = CloseStagingRepositoryRequest(repositoryId = repository.id)
    val response = api.close(profileId = repository.profileId, request = request).execute()
    if (response.isSuccessful.not()) {
      throw CliktError("Failed to close repository ${repository.id}")
    }
  }
}
