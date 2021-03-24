package nevam.nexus

import com.google.common.truth.Truth.assertThat
import nevam.MavenCoordinates
import nevam.NexusUser
import nevam.Pom
import nevam.network.NetworkModule
import org.junit.Test

class NexusModuleTest {

  @Test fun contentUrlLinksToMavenGroup() {
    val coordinates = MavenCoordinates("com.example", "nicolascage", "4.2.0")
    val pom = Pom(coordinates)
    val repo = StagingProfileRepository(
      id = "cagenicolas_1206",
      type = "closed",
      isTransitioning = false,
      updatedAtString = "Tue Aug 04 01:17:19 UTC 2020",
      profileId = "9000",
      profileName = "cage.nicolas"
    )
    val baseUrl = "https://s-test.oss.sonatype.org"
    val nexusModule = NexusModule(NetworkModule(debugMode = false), baseUrl, NexusUser("user", "pass"))
    assertThat(nexusModule.contentUrl(repo, pom)).isEqualTo(
      "$baseUrl/content/repositories/${repo.id}/${coordinates.mavenGroupDirectory()}"
    )
  }
}
