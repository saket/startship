package nevam

import nevam.util.GradleProperties

data class NexusUser(val username: String, val password: String) {
  companion object {
    fun readFrom(fileName: String): NexusUser {
      val properties = GradleProperties(fileName)
      return NexusUser(
          username = properties["SONATYPE_NEXUS_USERNAME"],
          password = properties["SONATYPE_NEXUS_PASSWORD"]
      )
    }
  }
}
