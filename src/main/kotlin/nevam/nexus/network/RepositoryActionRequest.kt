package nevam.nexus.network

import com.squareup.moshi.Json

@Suppress("DataClassPrivateConstructor")
data class RepositoryActionRequest private constructor(val data: Data) {
  constructor(repositoryId: String) : this(
      Data(repositoryId = repositoryId, description = null)
  )

  data class Data(
    @Json(name = "stagedRepositoryId")
    val repositoryId: String,

    @Json(name = "description")
    val description: String?
  )
}
