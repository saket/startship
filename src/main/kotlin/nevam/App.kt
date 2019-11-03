package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.TermUi.confirm
import nevam.nexus.NexusRepository
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) {
  val module = AppModule(
      user = readUserFromGradleProperties(),
      debugMode = false
  )
  App(module.nexusRepository).main(args)
}

class App(private val nexus: NexusRepository) : CliktCommand(name = "Nevam") {
  override fun run() {
    echo("Fetching staging repositories...")
    val staging = nexus.stagingRepository()
    echo(staging)

    staging.throwIfCannotBeClosed()

    val moduleName = "nevamtest"
    val versionName = "1.3.0"
    val contentUrl = staging.contentUrl(moduleName, versionName)
    echo("The contents of $moduleName $versionName can be checked here: \n$contentUrl\n")
    confirm(text = "Promote to release?", default = true, abort = true)
  }
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
