package nevam.nexus

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.TermUi.echo
import nevam.nexus.CloseStagingRepositoryRequest.Data

class RealNexus(private val api: NexusApi) : Nexus {

  @Throws(CliktError::class)
  override fun stagingRepositories(): List<StagingProfileRepository> {
    val response = api.stagingRepositories().execute()
    // TODO: handle user auth error.
    return when {
      response.isSuccessful -> response.body()!!.repositories
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
