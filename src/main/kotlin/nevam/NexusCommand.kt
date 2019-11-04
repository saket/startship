package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.CliktConsole
import com.github.ajalt.clikt.output.TermUi.confirm
import com.github.ajalt.clikt.output.TermUi.prompt
import nevam.clikt.UserInput
import nevam.nexus.Nexus
import nevam.nexus.StagingProfileRepository
import nevam.nexus.toTableString

class NexusCommand(
  private val nexus: Nexus,
  private val input: UserInput
) : CliktCommand(name = "Nevam") {

  override fun run() {
    echo("Fetching staged repositories...")
    val repositories = nexus.stagingRepositories()
    echo(repositories.toTableString())

    val selectedRepository = when (repositories.size) {
      0 -> throw CliktError("You don't have any staged repositories.")
      1 -> repositories.single()
      else -> promptUserToSelectARepository(repositories)
    }

    // TODO: If a repository is already closed, proceed
    //  to releasing it instead of throwing an error.
    selectedRepository.throwIfCannotBeClosed()

    val moduleName = "nevamtest"
    val versionName = "1.3.0"
    val contentUrl = selectedRepository.contentUrl(moduleName, versionName)
    echo("\nThe contents of $moduleName $versionName (${selectedRepository.id}) can be checked here: \n$contentUrl\n")
    input.confirm(text = "Close repository?", default = true, abort = true)
  }

  private fun promptUserToSelectARepository(options: List<StagingProfileRepository>): StagingProfileRepository {
    echo("You have multiple staged repositories.")
    while (true) {
      val promptHint = options.mapIndexed { i, _ -> i + 1 }.joinToString(separator = "/")
      val answer = input.prompt(text = "Enter a repository's number to proceed [$promptHint]")?.trim()
      val answerAsNumber = answer?.toIntOrNull()

      if (answerAsNumber != null) {
        if (answerAsNumber > options.size) {
          echo("\nYou selected $answerAsNumber, but you only have ${options.size} repositories. Let's try again?")
        } else {
          return options[answerAsNumber - 1]
        }
      } else {
        if (answer in options.map { it.id }) {
          return options.first { it.id == answer }
        } else {
          echo("\nNot sure what you meant by '$answer'. Let's try again by entering a row number from the table above?")
        }
      }
    }
  }
}
