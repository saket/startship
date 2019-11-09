package nevam.nexus

import com.jakewharton.fliptables.FlipTable
import com.squareup.moshi.Json
import nevam.Pom
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import nevam.nexus.network.ProfileId
import nevam.nexus.network.RepositoryId

data class StagingRepositoriesResponse(
  @Json(name = "data")
  val repositories: List<StagingProfileRepository>
)

data class StagingProfileRepository(
  @Json(name = "repositoryId")
  val id: RepositoryId,

  @Json(name = "profileName")
  val profileName: String,

  /**
   * ID of user's profile, which this repository belongs to.
   */
  @Json(name = "profileId")
  val profileId: ProfileId,

  @Json(name = "type")
  val type: String,

  @Json(name = "transitioning")
  private val isTransitioning: Boolean,

  @Json(name = "updatedDate")
  val updatedDate: String
) {

  companion object

  val status: Status by lazy {
    when {
      isTransitioning -> Transitioning
      else -> when {
        type.trim().equals("open", ignoreCase = true) -> Open
        type.trim().equals("closed", ignoreCase = true) -> Closed
        else -> Unknown(type)
      }
    }
  }

  fun contentUrl(pom: Pom): String {
    return "https://oss.sonatype.org/content/repositories/$id/${pom.mavenDirectory(includeVersion = true)}/"
  }

  sealed class Status(val displayValue: String) {
    object Open : Status("Open")
    object Closed : Status("Closed")
    object Transitioning : Status("Transitioning")
    data class Unknown(val value: String) : Status(value)
  }
}

fun Collection<StagingProfileRepository>.toTableString(): String {
  val printRowNumber = size > 1

  val headers = mutableListOf("Profile name", "Repository ID", "Status", "Updated at")
  if (printRowNumber) {
    headers.add(0, "")
  }

  val rows = mapIndexed { index, repo ->
    val row = mutableListOf(repo.profileName, repo.id, repo.status.displayValue, repo.updatedDate)
    if (printRowNumber) {
      row.add(0, "${index + 1}")
    }
    row.toTypedArray()
  }

  return FlipTable.of(headers.toTypedArray(), rows.toTypedArray())
}
