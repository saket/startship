package nevam

data class MavenCoordinates(
  /** e.g., "app.cash.paparazzi" */
  val groupId: String,

  /** e.g., "paparazzi" */
  val artifactId: String,

  /** e.g., "0.0.1" */
  val version: String
) {

  /** e.g., app.cash.paparazzi:paparazzi:0.0.1 */
  override fun toString(): String {
    return "$groupId:$artifactId:$version"
  }

  companion object {
    fun from(coordinates: String): MavenCoordinates {
      check(coordinates.count { it == ':' } == 2) {
        "Invalid coordinates $coordinates. Expected <groupId>:<artifactId>:<version>"
      }

      val (groupId, artifactId, version) = coordinates.split(":")
      return MavenCoordinates(groupId, artifactId, version)
    }
  }
}
