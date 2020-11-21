package nevam

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.output.defaultCliktConsole
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import io.reactivex.Observable
import io.reactivex.rxkotlin.blockingSubscribeBy
import nevam.clikt.UserInput
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
import nevam.util.hour
import nevam.util.stacktraceToString
import org.jline.terminal.TerminalBuilder.terminal
import kotlin.system.exitProcess

class ReleaseCommand : CliktCommand(name = "release") {
  private val debugMode by option("-d", "--debug", help = "Whether to print debug logs")
      .flag(default = false)

  private val coordinates by option("-c", "--coordinates", help = "Library's maven address")
      .convert { MavenCoordinates.from(it) }
      .defaultLazy { MavenCoordinates.readFrom("gradle.properties") }

  private val username by option(
      "-u", "--username",
      help = "The Sonatype Nexus username to use, or a global Gradle property defining it"
  ).default(NexusUser.DEFAULT_USERNAME_PROPERTY)

  private val password by option(
      "-p", "--password",
      help = "The Sonatype Nexus password to use, or a global Gradle property defining it"
  ).default(NexusUser.DEFAULT_PASSWORD_PROPERTY)

  private val appModule by lazy {
    val pomFromCoordinates = Pom(coordinates)
    val poms = if (pomFromCoordinates.artifactId.contains(',')) {
      val artifactIds = pomFromCoordinates.artifactId.split(',')
      List(artifactIds.size) {
        Pom(MavenCoordinates(pomFromCoordinates.groupId, artifactIds[it], pomFromCoordinates.version))
      }
    } else {
      listOf(pomFromCoordinates)
    }
    AppModule(
        user = NexusUser.readFrom("~/.gradle/gradle.properties", username, password),
        debugMode = debugMode,
        poms = poms
    )
  }

  private val input: UserInput = UserInput(defaultCliktConsole())
  private val nexus: Nexus get() = appModule.nexus
  private val poms: List<Pom> get() = appModule.poms

  override fun run() {
    echo("Preparing to release $coordinates")
    echoNewLine()
    echo("Fetching staged repositories...")
    val repositories = nexus.stagingRepositories()

    if (repositories.isNotEmpty()) {
      echo(repositories.toTableString())
    } else {
      throw CliktError("You don't have any staged repositories (╯°□°)╯︵ ┻━┻")
    }

    val selectedRepository = when (repositories.size) {
      0 -> throw CliktError("You don't have any staged repositories.")
      else -> promptUserToSelectARepository(repositories)
    }
    validate(selectedRepository)

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
    drop(selectedRepository)
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

  private fun validate(repository: StagingProfileRepository) {
    poms.forEach { pom ->
      val isMetadataPresent = nexus.isMetadataPresent(repository, pom).blockingGet()
      if (!isMetadataPresent) {
        echoNewLine()
        echo("Error: ${repository.id}'s maven coordinates don't match ${pom.coordinates}.")
        echo("Check if you uploaded an incorrect archive: https://oss.sonatype.org/#stagingRepositories.")
        throw CliktError("Aborted!")
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
    val pom = poms[0]
    val contentUrl = repository.contentUrl(pom)
    echo(
        """
          |The contents of ${pom.groupId} ${pom.version} (${repository.id}) can be verified here before it's released: 
          |$contentUrl
        """.trimMargin()
    )
    echoNewLine()
    input.confirm("Proceed to release? 🚢", default = true, abort = true)

    echoNewLine()
    echo("Jumping into hyperspace... ", trailingNewline = false)
    nexus.release(repository)
    echo("${pom.artifactId}:${pom.version} is released.")

    echo("Checking with Maven Central if it's available yet (can take an hour or two)...")
    waitTillAvailable().echoStreamingProgress()
  }

  private fun waitTillAvailable(): Observable<String> {
    val pom = poms[0]
    return nexus.pollUntilSyncedToMavenCentral(pom)
        .doAfterNext { if (it is GaveUp) exitProcess(1) }
        .map {
          when (it) {
            is Checking -> "Talking to Maven Central..."
            is WillRetry -> "Nope, not done yet"
            is RetryingIn -> "Checking again in ${it.secondsRemaining}s..."
            is Done -> "Available now. Go ahead and announce ${pom.artifactId}:${pom.version} to public!"
            is GaveUp -> {
              val emptyLineForCoveringLastEcho = Array(terminalWidth()) { " " }.joinToString(separator = "")
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
        onError = {
          echoNewLine()
          echo("Error: ${it.message}", err = true)
          if (debugMode) {
            echo(it.stacktraceToString())
          }
          exitProcess(1)
        },
        onNext = {
          // The line should be long enough to cover all characters of the last line.
          echo("\r${it.padEnd(terminalWidth(), padChar = ' ')}", trailingNewline = false)
        },
        onComplete = { echoNewLine(); echoNewLine() }
    )
  }

  private fun drop(repository: StagingProfileRepository) {
    echo("Dropping ${repository.id} in the meantime...")
    try {
      nexus.drop(repository)
      echo("Job's done.")

    } catch (e: Throwable) {
      echo("Failed. You can try doing it manually at https://oss.sonatype.org/#stagingRepositories")
    }
  }

  private fun echoNewLine() = echo("")
}

private fun Unit.exhaustive(): Any = this
private fun terminalWidth() = terminal().width
