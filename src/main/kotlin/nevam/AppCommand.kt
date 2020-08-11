@file:JvmName("App")

package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) {
  AppCommand()
      .subcommands(ReleaseCommand())
      .main(args)
}

class AppCommand : CliktCommand(name = "startship") {
  override fun run() = Unit
}
