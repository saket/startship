package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import nevam.clikt.UserInput
import nevam.nexus.Nexus
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
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

    with(selectedRepository) {
      when (status) {
        is Open, is Closed -> Unit
        is Transitioning -> throw CliktError("Repository $id is already transitioning to (probably) release.")
        is Unknown -> throw CliktError("Unknown status of repository: '$type'")
      }
    }

    if (selectedRepository.status is Open) {
      input.confirm(text = "Close repository ${selectedRepository.id}?", default = true, abort = true)
      echo("\nClosing ${selectedRepository.id}...")
      nexus.close(selectedRepository)

    } else {
      echo("TODO: Release an already closed repository.")
    }

    val moduleName = "nevamtest"
    val versionName = "1.3.0"
    val contentUrl = selectedRepository.contentUrl(moduleName, versionName)
    echo("\nThe contents of $moduleName $versionName (${selectedRepository.id}) can be checked here: \n$contentUrl\n")
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
          return options[answerAsNumber - 1].also {
            echo("")  // For a new line.
          }
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
