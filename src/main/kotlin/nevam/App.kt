package nevam

import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) {
  val module = AppModule(
      user = readUserFromGradleProperties(),
      debugMode = false
  )
  NexusCommand(module.nexusRepository).main(args)
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
