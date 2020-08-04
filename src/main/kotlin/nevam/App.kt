@file:JvmName("App")

package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import java.util.Properties as JavaProperties

fun main(args: Array<String>) {
  AppCommand().main(args)
}

class AppCommand : CliktCommand() {
  private val debugMode by option("-d", "--debug", help = "whether to print debug logs")
      .flag(default = false)

  private val coordinates by option("-c", "--coordinates", help = "library's maven address")
      .convert { MavenCoordinates.from(it) }
      .defaultLazy { MavenCoordinates.readFrom("gradle.properties") }

  override fun run() {
    val module = AppModule(
        user = NexusUser.readFrom("~/.gradle/gradle.properties"),
        debugMode = debugMode,
        pom = Pom(coordinates)
    )
    module.nexusCommand.main(emptyArray())
  }
}

private fun MavenCoordinates.Companion.readFrom(fileName: String): MavenCoordinates {
  try {
    val properties = Properties(fileName)
    return MavenCoordinates(
        groupId = properties["GROUP"],
        artifactId = properties["POM_ARTIFACT_ID"],
        version = properties["VERSION_NAME"]
    )
  } catch (ignored: Throwable) {
    throw CliktError(
        "Error: couldn't read maven coordinates from $fileName. You can pass them manually using -c option."
    )
  }
}

private fun NexusUser.Companion.readFrom(fileName: String): NexusUser {
  val properties = Properties(fileName)
  return NexusUser(
      username = properties["SONATYPE_NEXUS_USERNAME"],
      password = properties["SONATYPE_NEXUS_PASSWORD"]
  )
}

private class Properties(fileName: String) {
  private val file = File(fileName.replace("~", System.getProperty("user.home")))
  private val javaProperties = JavaProperties().apply {
    file.bufferedReader().use { load(it) }
  }

  operator fun get(key: String): String =
    javaProperties.getProperty(key)
        ?: throw CliktError("Error: $key not found in ${file.absolutePath}")
}
