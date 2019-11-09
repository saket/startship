package nevam

data class Pom(
  /** e.g., "app.cash.paparazzi" */
  val groupId: String,

  /** e.g., "paparazzi" */
  val artifactId: String,

  /** e.g., "0.0.1" */
  val version: String
) {

  /** e.g., app.cash.paparazzi:paparazzi:0.0.1 */
  val mavenAddress
    get() = "$groupId:$artifactId:$version"

  /** e.g., app/cash/paparazzi/paparazzi/0.0.1 */
  fun mavenDirectory(includeVersion: Boolean): String {
    val withoutVersion = "${groupId.replace(oldChar = '.', newChar = '/')}/$artifactId"
    return when {
      includeVersion -> "$withoutVersion/$version"
      else -> withoutVersion
    }
  }
}
