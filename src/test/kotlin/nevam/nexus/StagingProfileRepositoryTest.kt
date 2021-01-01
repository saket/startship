package nevam.nexus

import com.google.common.truth.Truth.assertThat
import nevam.MavenCoordinates
import nevam.Pom
import org.junit.Test

class StagingProfileRepositoryTest {
  @Test fun tableRenderingSingleRepoHidesIndex() {
    val repos = listOf(StagingProfileRepository(
        id = "cagenicolas_1206",
        type = "closed",
        isTransitioning = false,
        updatedAtString = "Tue Aug 04 01:17:19 UTC 2020",
        profileId = "9000",
        profileName = "cage.nicolas"
    ))
    assertThat(repos.toTableString()).isEqualTo("""
      |┌──────────────┬──────────────────┬────────┬──────────────────────────────┐
      |│ Profile name │ Repository ID    │ Status │ Update time                  │
      |├──────────────┼──────────────────┼────────┼──────────────────────────────┤
      |│ cage.nicolas │ cagenicolas_1206 │ Closed │ Tue Aug 04 01:17:19 UTC 2020 │
      |└──────────────┴──────────────────┴────────┴──────────────────────────────┘
      |""".trimMargin())
  }

  @Test fun tableRenderingMultipleReposShowsIndex() {
    val repos = listOf(
        StagingProfileRepository(
            id = "cagenicolas_1206",
            type = "closed",
            isTransitioning = false,
            updatedAtString = "Tue Aug 04 01:17:19 UTC 2020",
            profileId = "9000",
            profileName = "cage.nicolas"
        ),
        StagingProfileRepository(
            id = "cagenicolas_1207",
            type = "open",
            isTransitioning = false,
            updatedAtString = "Sun Aug 02 11:23:52 UTC 2020",
            profileId = "9000",
            profileName = "cage.nicolas"
        )
    )
    assertThat(repos.toTableString()).isEqualTo("""
      |    ┌──────────────┬──────────────────┬────────┬──────────────────────────────┐
      |    │ Profile name │ Repository ID    │ Status │ Update time                  │
      |┌───┼──────────────┼──────────────────┼────────┼──────────────────────────────┤
      |│ 1 │ cage.nicolas │ cagenicolas_1206 │ Closed │ Tue Aug 04 01:17:19 UTC 2020 │
      |├───┼──────────────┼──────────────────┼────────┼──────────────────────────────┤
      |│ 2 │ cage.nicolas │ cagenicolas_1207 │ Open   │ Sun Aug 02 11:23:52 UTC 2020 │
      |└───┴──────────────┴──────────────────┴────────┴──────────────────────────────┘
      |""".trimMargin())
  }

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
    assertThat(repo.contentUrl(pom))
        .isEqualTo("https://oss.sonatype.org/content/repositories/${repo.id}/${coordinates.mavenGroupDirectory()}/")
  }
}
