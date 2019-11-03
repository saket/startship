package nevam.nexus

import com.github.ajalt.clikt.core.CliktError

class NexusRepository(private val api: NexusApi) {

  fun stagingRepository(): StagingProfileRepository {
    val response = api.stagingRepositories().execute()
    if (response.isSuccessful) {
      val body = response.body()!!
      require(body.repositories.size == 1) {
        "Did not expect multiple staging repositories. Help the author add" +
            " support for this? https://github.com/saket/nevam/issues/new"
      }
      return body.repositories.single()
    }

    throw CliktError("Failed to connect to nexus")
  }
}
