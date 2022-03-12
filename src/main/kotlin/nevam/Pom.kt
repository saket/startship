package nevam

data class Pom(val coordinates: MavenCoordinates) {
  val groupId: String get() = coordinates.groupId
  val artifactId: String get() = coordinates.artifactId
  val version: String get() = coordinates.version
}
