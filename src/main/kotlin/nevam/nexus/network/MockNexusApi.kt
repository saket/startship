package nevam.nexus.network

import com.slack.eithernet.ApiResult
import kotlinx.coroutines.delay
import nevam.Pom
import nevam.nexus.NexusConfig
import nevam.nexus.NexusConfig.StatusCheck
import nevam.nexus.StagingProfileRepository
import nevam.nexus.StagingRepositoriesResponse
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
val MOCK_NEXUS_CONFIG = NexusConfig(
  closedStatusCheck = StatusCheck(
    giveUpAfter = 10.minutes,
    initialRetryDelay = 2.seconds,
    backoffFactor = 1.5f
  ),
  releasedStatusCheck = StatusCheck(
    giveUpAfter = 2.hours,
    initialRetryDelay = 2.seconds,
    backoffFactor = 1.5f
  )
)

/**
 * Used for testing on the CLI.
 */
@Suppress("unused")
class MockNexusApi(private val pom: Pom) : NexusApi {
  override fun stagingRepositories(): ApiResult<StagingRepositoriesResponse, Unit> {
    return ApiResult.success(
      StagingRepositoriesResponse(
        listOf(
          //              StagingProfileRepository(
          //                  profileName = "me.saket (fake)",
          //                  id = "mesaket-1042",
          //                  type = "closed",
          //                  updatedDate = "Wed Nov 06 00:50:51 UTC 2019",
          //                  profileId = "999",
          //                  isTransitioning = false
          //              ),
          StagingProfileRepository(
            id = "mesaket-1047",
            type = "open",
            updatedAtString = "Tue Aug 04 01:17:19 UTC 2020",
            profileId = "999",
            profileName = "me.saket (fake)",
            isTransitioning = false
          )
        )
      )
    )
  }

  override suspend fun stagingMavenMetadata(
    repositoryId: RepositoryId,
    repositoryPath: String
  ): ApiResult<MavenMetadata, Unit> {
    return ApiResult.success(
      MavenMetadata(
        MavenMetadata.Data(
          groupId = "foo",
          artifactId = "foo",
          versions = MavenMetadata.Versions(
            release = "foo",
            lastUpdatedDate = "foo"
          )
        )
      )
    )
  }

  override fun close(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    Thread.sleep(500)
    return ApiResult.success(Unit)
  }

  private var closedStatusRetryCount = -1
  override suspend fun stagingRepository(repositoryId: RepositoryId): ApiResult<StagingProfileRepository, Unit> {
    delay(250.milliseconds)

    return ApiResult.success(
      if (closedStatusRetryCount++ >= 2) {
        StagingProfileRepository(
          id = "mesaket-1042",
          type = "closed",
          updatedAtString = "Wed Nov 06 00:50:51 UTC 2019",
          profileId = "999",
          profileName = "me.saket",
          isTransitioning = false
        )
      } else {
        StagingProfileRepository(
          id = "mesaket-1042",
          type = "open",
          updatedAtString = "Wed Nov 06 00:50:51 UTC 2019",
          profileId = "999",
          profileName = "me.saket",
          isTransitioning = true
        )
      }
    )
  }

  override fun release(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    Thread.sleep(500)
    return ApiResult.success(Unit)
  }

  override fun drop(profileId: ProfileId, request: RepositoryActionRequest): ApiResult<Unit, Unit> {
    Thread.sleep(500)
    return ApiResult.success(Unit)
  }

  private var releasedStatusRetryCount = -1
  override suspend fun releaseMavenMetadata(repositoryPath: String): ApiResult<MavenMetadata, Unit> {
    delay(250.milliseconds)

    return ApiResult.success(
      if (releasedStatusRetryCount++ >= 2) {
        MavenMetadata(
          MavenMetadata.Data(
            groupId = pom.groupId,
            artifactId = pom.artifactId,
            versions = MavenMetadata.Versions(
              release = pom.version,
              lastUpdatedDate = "20191108065802"
            )
          )
        )
      } else {
        MavenMetadata(
          MavenMetadata.Data(
            groupId = pom.groupId,
            artifactId = pom.artifactId,
            versions = MavenMetadata.Versions(
              release = "${pom.version}-old",
              lastUpdatedDate = "20191108065801"
            )
          )
        )
      }
    )
  }
}
