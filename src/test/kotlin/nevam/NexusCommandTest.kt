package nevam

import com.google.common.truth.Truth.assertThat
import nevam.clikt.UserInput
import nevam.nexus.FakeNexus
import nevam.nexus.StagingProfileRepository
import org.junit.Ignore
import org.junit.Test

class NexusCommandTest {

  private val nexus = FakeNexus
  private val console = FakeCliktConsole()
  private val app = NexusCommand(
      nexus = nexus,
      input = UserInput(console),
      pom = Pom(
          groupId = "cage.nicolas",
          artifactId = "ghostrider",
          version = "1.0.0"
      )
  )

  private val fakeRepository = StagingProfileRepository.FAKE

  private fun runApp() {
    // main() catches CliktErrors. parse() does not.
    app.parse(argv = arrayOf())
  }

  @Ignore
  @Test fun `closing a non-open staged repository fails`() {
    val assertError = { message: String ->
      assertThat(expectError { runApp() })
          .hasMessageThat()
          .contains(message)
    }

    nexus.repository = fakeRepository.copy(type = "closed")
    assertError("cannot be closed as it's already closed")

    nexus.repository = fakeRepository.copy(type = "unknown_type")
    assertError("Unknown status of repository: 'unknown_type'")

    nexus.repository = fakeRepository.copy(type = "open", isTransitioning = true)
    assertError("is already transitioning to (probably) release")

    nexus.repository = fakeRepository.copy(type = "closed", isTransitioning = true)
    assertError("is already transitioning to (probably) release")
  }

  @Ignore
  @Test fun `selection from multiple staged repositories`() {
    nexus.repositories = listOf(
        fakeRepository.copy(id = "mesaket-1026", type = "open"),
        fakeRepository.copy(id = "mesaket-1027", type = "closed")
    )

    console.userInputs["Enter a repository's number to proceed [1/2]: "] = "1"

    runApp()

    TODO()
  }
}
