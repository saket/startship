package nevam.nexus

import com.google.common.truth.Truth.assertThat
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
      |│ 0 │ cage.nicolas │ cagenicolas_1206 │ Closed │ Tue Aug 04 01:17:19 UTC 2020 │
      |├───┼──────────────┼──────────────────┼────────┼──────────────────────────────┤
      |│ 1 │ cage.nicolas │ cagenicolas_1207 │ Open   │ Sun Aug 02 11:23:52 UTC 2020 │
      |└───┴──────────────┴──────────────────┴────────┴──────────────────────────────┘
      |""".trimMargin())
  }
}
