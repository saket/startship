package nevam.nexus.network

import com.squareup.moshi.Json

data class MavenMetadata(
  @Json(name = "metadata")
  val data: Data
) {

  val releaseVersion
    get() = data.versions.release

  data class Data(
    @Json(name = "groupId")
    val groupId: String,

    @Json(name = "artifactId")
    val artifactId: String,

    @Json(name = "versioning")
    val versions: Versions
  )

  data class Versions(
    @Json(name = "release")
    val release: String,

    @Json(name = "lastUpdated")
    val lastUpdatedDate: String
  )
}
