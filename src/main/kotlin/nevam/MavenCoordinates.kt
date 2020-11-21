package nevam

import com.github.ajalt.clikt.core.CliktError
import nevam.util.GradleProperties
import java.io.File

data class MavenCoordinates(
  /** e.g., "app.cash.paparazzi" */
  val groupId: String,

  /** e.g., "paparazzi" */
  val artifactId: String,

  /** e.g., "0.0.1" */
  val version: String
) {

  /** e.g., app.cash.paparazzi:paparazzi:0.0.1 */
  override fun toString(): String {
    return "$groupId:$artifactId:$version"
  }

  /** e.g., app/cash/paparazzi/paparazzi/0.0.1 */
  fun mavenDirectory(includeVersion: Boolean): String {
    val withoutVersion = "${mavenGroupDirectory()}/$artifactId"
    return when {
      includeVersion -> "$withoutVersion/$version"
      else -> withoutVersion
    }
  }

  /** e.g., app/cash/paparazzi */
  fun mavenGroupDirectory(): String {
    return groupId.replace(oldChar = '.', newChar = '/')
  }

  companion object {
    fun from(coordinates: String): MavenCoordinates {
      check(coordinates.count { it == ':' } == 2) {
        "Invalid coordinates $coordinates. Expected <groupId>:<artifactId>:<version>"
      }

      val (groupId, artifactId, version) = coordinates.split(":")
      return MavenCoordinates(groupId, artifactId, version)
    }

    fun readFrom(fileName: String): MavenCoordinates {
      try {
        val properties = GradleProperties(fileName)
        return MavenCoordinates(
            groupId = properties["GROUP"],
            artifactId = properties["POM_ARTIFACT_ID"],
            version = properties["VERSION_NAME"]
        )
      } catch (ignored: Throwable) {
        val filePath = File(fileName).absolutePath.replace(System.getProperty("user.home"), "~")
        throw CliktError(
            "Error: couldn't read maven coordinates from this directory ($filePath)." +
                " You can pass them manually using -c option: `-c group:artifact:version`"
        )
      }
    }
  }
}
