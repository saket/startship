package nevam.util

import com.github.ajalt.clikt.core.CliktError
import java.io.File
import java.util.Properties

class GradleProperties(fileName: String) {
  private val file = File(fileName.replace("~", System.getProperty("user.home")))
  private val javaProperties = Properties().apply {
    file.bufferedReader().use { load(it) }
  }

  operator fun get(key: String): String =
    javaProperties.getProperty(key)
        ?: throw CliktError("Error: $key not found in ${file.absolutePath}")
}
