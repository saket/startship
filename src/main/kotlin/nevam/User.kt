package nevam

import nevam.util.GradleProperties

data class NexusUser(val username: String, val password: String) {
  companion object {
    fun readFrom(
        fileName: String,
        usernameProperty: String = DEFAULT_USERNAME_PROPERTY, passwordProperty: String = DEFAULT_PASSWORD_PROPERTY
    ): NexusUser {
      val properties = GradleProperties(fileName)
      return NexusUser(
          username = properties[usernameProperty],
          password = properties[passwordProperty]
      )
    }

    const val DEFAULT_USERNAME_PROPERTY: String = "SONATYPE_NEXUS_USERNAME"
    const val DEFAULT_PASSWORD_PROPERTY: String = "SONATYPE_NEXUS_PASSWORD"
  }
}
