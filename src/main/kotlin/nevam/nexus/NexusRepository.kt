package nevam.nexus

import com.jakewharton.fliptables.FlipTable

class NexusRepository(private val api: NexusApi) {

  fun stagingRepository(): String {
    val response = api.stagingRepositories().execute()
    return when {
      response.isSuccessful -> asciiTable(response.body()!!.data)
      else -> "Failed to connect to nexus"
    }
  }

  private fun asciiTable(repositories: List<StagingProfileRepository>): String {
    require(repositories.size == 1) {
      "Did not expect multiple staging repositories. Help the author add" +
          " support for this? https://github.com/saket/nevam/issues/new"
    }

    val repository = repositories.single()
    val headers = arrayOf("Profile name", "Repository ID", "Updated at")
    val row = arrayOf(repository.profileName, repository.id, repository.updatedDate)
    return FlipTable.of(headers, arrayOf(row))
  }
}
