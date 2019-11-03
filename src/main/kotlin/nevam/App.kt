package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.confirm
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
    echo("Fetching staging repositories…")
    val staging = module.nexusRepository.stagingRepository()
    echo(staging)

    val proceed = confirm(text = "Promote to release?", default = true)
    echo("Answered: $proceed")
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

