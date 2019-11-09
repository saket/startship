package nevam

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PomTest {

  private val pom = Pom(
      groupId = "me.saket",
      artifactId = "flick",
      version = "1.8.0"
  )

  @Test fun `maven address`() {
    assertThat(pom.mavenAddress).isEqualTo("me.saket:flick:1.8.0")
  }

  @Test fun `maven directory`() {
    assertThat(pom.mavenDirectory(includeVersion = false)).isEqualTo("me/saket/flick")
    assertThat(pom.mavenDirectory(includeVersion = true)).isEqualTo("me/saket/flick/1.8.0")
  }
}
