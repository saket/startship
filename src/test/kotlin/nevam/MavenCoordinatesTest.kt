package nevam

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MavenCoordinatesTest {

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
}
