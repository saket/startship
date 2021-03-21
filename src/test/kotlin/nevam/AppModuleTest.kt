package nevam

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppModuleTest {

  @Test fun `constructor with no hostPrefix produces standard Nexus URL`() {
    val module = AppModule(
      user = NexusUser("user", "pass"),
      debugMode = false,
      poms = emptyList()
    )
    assertThat(module.nexusModule.repositoryUrl).isEqualTo("https://oss.sonatype.org")
  }

  @Test fun `constructor with hostPrefix produces prefixed Nexus URL`() {
    val module = AppModule(
      user = NexusUser("user", "pass"),
      debugMode = false,
      poms = emptyList(),
      hostPrefix = "test-prefix"
    )
    assertThat(module.nexusModule.repositoryUrl).isEqualTo("https://test-prefix.oss.sonatype.org")
  }
}
