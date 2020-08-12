package nevam.nexus

import com.jakewharton.picnic.renderText
import com.jakewharton.picnic.table
import com.squareup.moshi.Json
import nevam.Pom
import nevam.nexus.StagingProfileRepository.Status.Closed
import nevam.nexus.StagingProfileRepository.Status.Open
import nevam.nexus.StagingProfileRepository.Status.Released
import nevam.nexus.StagingProfileRepository.Status.Transitioning
import nevam.nexus.StagingProfileRepository.Status.Unknown
import nevam.nexus.network.ProfileId
import nevam.nexus.network.RepositoryId
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.LazyThreadSafetyMode.NONE

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
  val updatedAtString: String
) {

  val status: Status by lazy(NONE) {
    when {
      isTransitioning -> Transitioning
      else -> when {
        type.trim().equals("open", ignoreCase = true) -> Open
        type.trim().equals("closed", ignoreCase = true) -> Closed
        type.trim().equals("released", ignoreCase = true) -> Released
        else -> Unknown(type)
      }
    }
  }

  fun contentUrl(pom: Pom): String {
    return "https://oss.sonatype.org/content/repositories/$id/${pom.coordinates.mavenDirectory(includeVersion = true)}/"
  }

  sealed class Status(val displayValue: String) {
    object Open : Status("Open")
    object Closed : Status("Closed")
    object Released : Status("Released")
    object Transitioning : Status("Transitioning")
    data class Unknown(val value: String) : Status(value)
  }
}

fun Collection<StagingProfileRepository>.toTableString(): String {
  val printRowNumber = size > 1
  val table = table {
    cellStyle {
      border = true
      paddingLeft = 1
      paddingRight = 1
    }
    row {
      if (printRowNumber) {
        cell("") {
          borderTop = false
          borderLeft = false
        }
      }
      cells("Profile name", "Repository ID", "Status", "Update time")
    }
    mapIndexed { index, repo ->
      row {
        if (printRowNumber) {
          cell(index)
        }
        cells(repo.profileName, repo.id, repo.status.displayValue, repo.timestampRelativeToNow())
      }
    }
  }
  return table.renderText()
}

fun StagingProfileRepository.timestampRelativeToNow(clock: Clock = Clock.systemUTC()): String {
  val formatter = DateTimeFormatter.ofPattern("eee MMM dd HH:mm:ss 'UTC' yyyy")
  val updatedAt = LocalDateTime.parse(updatedAtString, formatter)
  val timeSince = Duration.between(updatedAt, LocalDateTime.now(clock))

  return when {
    timeSince < Duration.ofMinutes(1) -> {
      "${timeSince.seconds}s ago"
    }
    timeSince < Duration.ofHours(1) -> {
      val mins = timeSince.toMinutes()
      val seconds = timeSince.minusMinutes(mins).seconds
      "${mins}m ${seconds}s ago"
    }
    timeSince < Duration.ofDays(1) -> {
      val hours = timeSince.toHours()
      val minutes = timeSince.minusHours(hours).toMinutes()
      "${hours}h ${minutes}m ago"
    }
    else -> updatedAtString
  }
}
