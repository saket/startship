@file:JvmName("App")

package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.double
import java.io.FileInputStream
import java.util.Properties

fun main(args: Array<String>) {
  AppCommand().main(args)
}

class AppCommand : CliktCommand() {
  private val debugMode by option("-d", "--debug", help = "whether to print debug logs")
      .flag(default = false)

  private val coordinates by option("-c", "--coordinates", help = "library's maven address")
      .convert { MavenCoordinates.from(it) }
      .required()

  override fun run() {
    val module = AppModule(
        user = readUserFromGradleProperties(),
        debugMode = debugMode,
        pom = Pom(coordinates)
    )
    module.nexusCommand.main(emptyArray())
  }
}

private fun readUserFromGradleProperties(): NexusUser {
  val property = { name: String ->
    val input = FileInputStream("/Users/saket/.gradle/gradle.properties")
    val prop = Properties().apply { load(input) }
    prop.getProperty(name) ?: error("$name not found in ~/.gradle/gradle.properties")
  }
  return NexusUser(
      username = property("SONATYPE_NEXUS_USERNAME"),
      password = property("SONATYPE_NEXUS_PASSWORD")
  )
}
