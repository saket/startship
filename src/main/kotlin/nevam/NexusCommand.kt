package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import io.reactivex.Observable
import io.reactivex.rxkotlin.blockingSubscribeBy
import nevam.clikt.UserInput
import nevam.extensions.hour
import nevam.nexus.Nexus
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Released
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
  private val input: UserInput,
  private val pom: Pom
) : CliktCommand(name = "Nevam") {

  override fun run() {
    echo("Fetching staged repositories...")
    val repositories = nexus.stagingRepositories()

    if (repositories.isNotEmpty()) {
      echo(repositories.toTableString())
    } else {
      throw CliktError("You don't have any staged repositories (╯°□°)╯︵ ┻━┻")
    }

    val selectedRepository = when (repositories.size) {
      0 -> throw CliktError("You don't have any staged repositories.")
      1 -> repositories.single()
      else -> promptUserToSelectARepository(repositories)
    }

    with(selectedRepository) {
      when (status) {
        is Open, is Closed -> Unit
        is Released -> throw CliktError("Repository $id is already released.")
        is Transitioning -> throw CliktError("Repository $id is already transitioning to (probably) release.")
        is Unknown -> throw CliktError("Unknown status of repository: '$type'")
      }.exhaustive()
    }

    if (selectedRepository.status is Open) {
      close(selectedRepository)
    }
    release(selectedRepository)
    dropSilently(selectedRepository)
  }

  private fun promptUserToSelectARepository(options: List<StagingProfileRepository>): StagingProfileRepository {
    if (options.size == 1) {
      return options.single()
    }

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

  private fun close(repository: StagingProfileRepository) {
    input.confirm("Close repository ${repository.id}?", default = true, abort = true)
    echoNewLine()
    echo("Requesting Nexus to mark ${repository.id} as closed... ", trailingNewline = false)
    nexus.close(repository)
    echo("done.")

    echo("Confirming with Nexus if it's closed yet...")
    waitTillClosed(repository).echoStreamingProgress()
  }

  private fun waitTillClosed(repository: StagingProfileRepository): Observable<String> {
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

  private fun release(repository: StagingProfileRepository) {
    val contentUrl = repository.contentUrl(pom)
    echo(
        """
          |The contents of ${pom.artifactId} ${pom.version} (${repository.id}) can be verified here before it's released: 
          |$contentUrl
        """.trimMargin()
    )
    echoNewLine()
    input.confirm("Proceed to release? 🚀", default = true, abort = true)

    echoNewLine()
    echo("Jumping into hyperspace... ", trailingNewline = false)
    nexus.release(repository)
    echo("${pom.artifactId}.${pom.version} is released.")

    echo("Checking with Maven Central if it's available yet (can take an hour or two)...")
    waitTillAvailable().echoStreamingProgress()
  }

  private fun waitTillAvailable(): Observable<String> {
    return nexus.pollUntilSyncedToMavenCentral(pom)
        .doAfterNext { if (it is GaveUp) exitProcess(1) }
        .map {
          when (it) {
            is Checking -> "Talking to Maven Central..."
            is WillRetry -> "Nope, not done yet"
            is RetryingIn -> "Checking again in ${it.secondsRemaining}s..."
            is Done -> "Available now. Go ahead and announce ${pom.artifactId} ${pom.version} to public!"
            is GaveUp -> {
              val emptyLineForCoveringLastEcho = Array(100) { " " }.joinToString(separator = "")
              val timeSpent = when {
                it.after >= 1.hour -> "${it.after.toHours()} hours"
                else -> "${it.after.toMinutes()} minutes"
              }
              """
              |$emptyLineForCoveringLastEcho
              |Gave up after trying for $timeSpent. It usually doesn't take this long, and is 
              |probably an indication that Nexus is having issues today. Try again after some time?
              """.trimMargin()
            }
          }
        }
  }

  private fun Observable<String>.echoStreamingProgress() {
    blockingSubscribeBy(
        onError = { echo(it.message, err = true); exitProcess(1) },
        // '\r' moves the cursor to the beginning of the line so
        // that the status message can be updated on the same line.
        onNext = { echo("\r$it", trailingNewline = false) },
        onComplete = { echoNewLine(); echoNewLine() }
    )
  }

  private fun dropSilently(repository: StagingProfileRepository) {
    echo("TODO: drop ${repository.id} repository in background")
  }

  private fun echoNewLine() = echo("")
}

private fun Unit.exhaustive(): Any = this
