package nevam.nexus

import com.jakewharton.fliptables.FlipTable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import nevam.nexus.StagingProfileRepository.Type.Open
import nevam.nexus.StagingProfileRepository.Type.Unknown
import java.time.Instant

data class StagingRepositoriesResponse(
  @Json(name = "data")
  val repositories: List<StagingProfileRepository>
)

data class StagingProfileRepository(
  @Json(name = "repositoryId")
  val id: String,

  @Json(name = "profileName")
  val profileName: String,

  @Json(name = "type")
  val _type: String,

  @Json(name = "updatedDate")
  val updatedDate: String
) {

  val type: Type by lazy {
    when(_type) {
      "open" -> Open
      else -> Unknown(_type)
    }
  }

  override fun toString(): String {
    val headers = arrayOf("Profile name", "Repository ID", "Type", "Updated at")
    val row = arrayOf(profileName, id, _type, updatedDate)
    return FlipTable.of(headers, arrayOf(row))
  }

  sealed class Type {
    object Open : Type()
    data class Unknown(val value: String): Type()
  }
}
