package nevam

import com.github.ajalt.clikt.core.CliktError
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class MavenCoordinatesTest {

  @Rule
  @JvmField val thrown: ExpectedException = ExpectedException.none()

  @Test fun `extract coordinates parts`() {
    val coordinates = MavenCoordinates.from("com.squareup.sqldelight:runtime:1.4.0")
    assertThat(coordinates.groupId).isEqualTo("com.squareup.sqldelight")
    assertThat(coordinates.artifactId).isEqualTo("runtime")
    assertThat(coordinates.version).isEqualTo("1.4.0")
    assertThat(coordinates.toString()).isEqualTo("com.squareup.sqldelight:runtime:1.4.0")
  }

  @Test fun `construct directory path`() {
    val coordinates = MavenCoordinates.from("com.squareup.sqldelight:runtime:1.4.0")
    assertThat(coordinates.mavenDirectory(includeVersion = false)).isEqualTo("com/squareup/sqldelight/runtime")
    assertThat(coordinates.mavenDirectory(includeVersion = true)).isEqualTo("com/squareup/sqldelight/runtime/1.4.0")
  }

  @Test fun `read properties from file with all properties succeeds`() {
    val coordinates = MavenCoordinates.readFrom("src/test/resources/full.gradle.properties")
    assertThat(coordinates.toString()).isEqualTo("com.squareup.sqldelight:runtime:1.4.0")
  }

  @Test fun `read properties from file with missing properties suggests -c`() {
    thrown.expect(CliktError::class.java)
    thrown.expectMessage("`-c group:artifact:version`")
    MavenCoordinates.readFrom("src/test/resources/missing.gradle.properties")
  }
}
