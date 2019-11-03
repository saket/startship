package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.confirm
import nevam.nexus.NexusRepository
import java.io.FileInputStream
import java.util.Properties

class NexusCommand(private val nexus: NexusRepository) : CliktCommand(name = "Nevam") {
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
