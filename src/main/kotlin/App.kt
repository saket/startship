import com.github.ajalt.clikt.core.CliktCommand

fun main(args: Array<String>) = App().main(args)

class App : CliktCommand(name = "Nevam") {
  override fun run() {
    echo("Sup?")
  }
}
