package nevam

import nevam.util.GradleProperties

data class NexusUser(val username: String, val password: String) {
  companion object {
    fun readFrom(
        fileName: String,
        username: String = DEFAULT_USERNAME_PROPERTY,
        password: String = DEFAULT_PASSWORD_PROPERTY
    ): NexusUser {
      val properties = GradleProperties(fileName)
      val actualUsername = if (username in properties) properties[username] else username
      val actualPassword = if (password in properties) properties[password] else password
      return NexusUser(
          username = actualUsername,
          password = actualPassword
      )
    }

    const val DEFAULT_USERNAME_PROPERTY: String = "mavenCentralUsername"
    const val DEFAULT_PASSWORD_PROPERTY: String = "mavenCentralPassword"
  }
}
