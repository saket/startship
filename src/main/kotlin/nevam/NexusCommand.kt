package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import io.reactivex.Observable
import io.reactivex.rxkotlin.blockingSubscribeBy
import nevam.clikt.UserInput
import nevam.nexus.Nexus
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import nevam.nexus.StatusCheckState.Checking
import nevam.nexus.StatusCheckState.Done
import nevam.nexus.StatusCheckState.GaveUp
import nevam.nexus.StatusCheckState.RetryingIn
import nevam.nexus.StatusCheckState.WillRetry
import nevam.nexus.toTableString
import kotlin.system.exitProcess

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
      close(selectedRepository)
    }
    release(selectedRepository)
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

  private fun waitTillClosed(repository: StagingProfileRepository): Observable<String> {
    echo("Confirming with Nexus if it's closed yet...")

    return nexus.pollUntilClosed(repository.id)
        .doAfterNext { if (it is GaveUp) exitProcess(1) }
        .map {
          when (it) {
            is Checking -> "Talking to Nexus..."
            is WillRetry -> "Nope, not done yet"
            is RetryingIn -> "Checking again in ${it.secondsRemaining}s..."
            is Done -> "Closed!"
            is GaveUp -> {
              val emptyLineForCoveringLastEcho = Array(100) { " " }.joinToString(separator = "")
              """
              |$emptyLineForCoveringLastEcho
              |Gave up after trying for ${it.after.toMinutes()} minutes. It usually doesn't take this long, and is 
              |probably an indication that Nexus is unavailable. Try again after some time?
              """.trimMargin()
            }
          }
        }
  }

  private fun close(repository: StagingProfileRepository) {
    input.confirm("Close repository ${repository.id}?", default = true, abort = true)
    echoNewLine()
    echo("Requesting Nexus to mark ${repository.id} as closed... ", trailingNewline = false)
    nexus.close(repository)
    echo("done.")

    waitTillClosed(repository).blockingSubscribeBy(
        onError = { echo(it.message, err = true); exitProcess(1) },
        // '\r' moves the cursor to the beginning of the line so
        // that the status message can be updated on the same line.
        onNext = { echo("\r$it", trailingNewline = false) },
        onComplete = { echoNewLine(); echoNewLine() }
    )
  }

  private fun release(repository: StagingProfileRepository) {
    // TODO: get module name and version from the user or auto-read from somewhere.
    val moduleName = "nevamtest"
    val moduleVersion = "1.3.0"

    val contentUrl = repository.contentUrl(moduleName, moduleVersion)
    echo("The contents of $moduleName $moduleVersion (${repository.id}) can be checked here before it's closed: \n$contentUrl\n")
    input.confirm("Proceed to release? 🚀", default = true, abort = true)

    echoNewLine()
    echo("Jumping into hyperspace... ", trailingNewline = false)
    nexus.release(repository)
    echo("done.")

    echoNewLine()
    echo("TODO: drop released repository in background")
  }

  private fun echoNewLine() = echo("")
}

private fun Unit.exhaustive(): Any = this
