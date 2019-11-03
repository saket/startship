package nevam

import com.google.common.truth.Truth.assertThat
import nevam.nexus.FakeNexusRepository
import nevam.nexus.StagingProfileRepository
import org.junit.Test

class AppTest {

  private val nexus = FakeNexusRepository
  private val app = App(nexus)

  private fun runApp() {
    // main() catches CliktErrors. parse() does not.
    app.parse(argv = arrayOf())
  }

  @Test fun `closing a non-open staged repository fails`() {
    val fakeRepository = StagingProfileRepository(
        id = "cagenicolas_1206",
        profileName = "cage.nicolas",
        type = "closed",
        isTransitioning = false,
        updatedDate = "Sometime"
    )
    val assertError = { message: String ->
      assertThat(expectError { runApp() })
          .hasMessageThat()
          .contains(message)
    }

    nexus.repository = fakeRepository.copy(type = "closed")
    assertError("cannot be promoted as it's already closed")

    nexus.repository = fakeRepository.copy(type = "unknown_type")
    assertError("Unknown status of repository: 'unknown_type'")

    nexus.repository = fakeRepository.copy(type = "open", isTransitioning = true)
    assertError("is already transitioning to (probably) release")

    nexus.repository = fakeRepository.copy(type = "closed", isTransitioning = true)
    assertError("is already transitioning to (probably) release")
  }
}
