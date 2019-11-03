package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.TermUi.confirm
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) = App().main(args)

class App : CliktCommand(name = "Nevam") {
  private val debugMode = false

  override fun run() {
    val module = AppModule(
        user = readUserFromGradleProperties(),
        debugMode = debugMode
    )
    echo("Fetching staging repositories...")
    val staging = module.nexusRepository.stagingRepository()
    echo(staging)

    if (staging.status !is Open) {
      throw CliktError(cannotCloseError(staging))
    }

    val moduleName = "nevamtest"
    val versionName = "1.3.0"
    val contentUrl = staging.contentUrl(moduleName, versionName)
    echo("The contents of $moduleName $versionName can be checked here: \n$contentUrl\n")
    confirm(text = "Promote to release?", default = true, abort = true)
  }

  private fun cannotCloseError(staging: StagingProfileRepository): String {
    return when(staging.status) {
      is Open -> error("impossible")
      is Closed -> "Repository ${staging.profileName} cannot be promoted as it's already closed."
      is Transitioning -> "Repository ${staging.profileName} is already transitioning to (probably) release."
      is Unknown -> "Unknown status of repository ${staging.profileName}"
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
}

