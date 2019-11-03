package nevam.nexus

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

data class StagingRepositoriesResponse(
  @Json(name = "data")
  val data: List<StagingProfileRepository>
)

data class StagingProfileRepository(

  @Json(name = "repositoryId")
  val id: String,

  @Json(name = "profileName")
  val profileName: String,

  @Json(name = "updatedDate")
  val updatedDate: String
)
