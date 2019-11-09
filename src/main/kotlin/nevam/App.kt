@file:JvmName("App")

package nevam

import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) {
  // TODO: get from the user or auto-read from somewhere:
  //  1. POM
  //  2. User credentials
  val module = AppModule(
      user = readUserFromGradleProperties(),
      debugMode = false,
      pom = Pom(
          groupId = "me.saket",
          artifactId = "nevamtest",
          version = "1.3.4"
      )
  )
  module.nexusCommand.main(args)
}

private fun readUserFromGradleProperties(): NexusUser {
  val property = { name: String ->
    val input = FileInputStream("/Users/saket/.gradle/gradle.properties")
    val prop = Properties().apply { load(input) }
    prop.getProperty(name)
  }
  return NexusUser(
      username = property("SONATYPE_NEXUS_USERNAME"),
      password = property("SONATYPE_NEXUS_PASSWORD")
  )
}
