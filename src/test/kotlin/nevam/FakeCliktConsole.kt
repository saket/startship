package nevam

import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.NonInteractiveCliktConsole

typealias Prompt = String
typealias Input = String

class FakeCliktConsole: CliktConsole {

  private val delegate = NonInteractiveCliktConsole()
  val userInputs = mutableMapOf<Prompt, Input>()

  override val lineSeparator = delegate.lineSeparator

  override fun print(text: String, error: Boolean) {
    delegate.print(text, error)
  }

  override fun promptForLine(prompt: String, hideInput: Boolean): String? {
    when (prompt) {
      in userInputs -> return userInputs.remove(prompt)
      else -> throw AssertionError("No input for '$prompt'")
    }
  }
}
