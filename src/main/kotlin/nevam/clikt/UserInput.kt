package nevam.clikt

import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.TermUi

class UserInput(private val console: CliktConsole) {
  fun prompt(text: String): String? {
    return TermUi.prompt(text, console = console, convert = { it })
  }

  fun confirm(text: String, default: Boolean = false, abort: Boolean = false) {
    TermUi.confirm(text = text, default = default, abort = abort)
  }
}
