package nevam

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PomTest {

  private val pom = Pom(MavenCoordinates.from("me.saket:flick:1.8.0"))

  @Test fun `maven address`() {
    assertThat(pom.coordinates.toString()).isEqualTo("me.saket:flick:1.8.0")
  }

  @Test fun `maven directory`() {
    assertThat(pom.mavenDirectory(includeVersion = false)).isEqualTo("me/saket/flick")
    assertThat(pom.mavenDirectory(includeVersion = true)).isEqualTo("me/saket/flick/1.8.0")
  }
}
