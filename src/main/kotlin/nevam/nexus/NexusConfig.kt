package nevam.nexus

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class NexusConfig(
  val closedStatusCheck: StatusCheck,
  val releasedStatusCheck: StatusCheck
) {
  data class StatusCheck(
    val giveUpAfter: Duration,
    val initialRetryDelay: Duration,
    val backoffFactor: Float
  )

  companion object {
    val DEFAULT = NexusConfig(
      closedStatusCheck = StatusCheck(
        giveUpAfter = 10.minutes,
        initialRetryDelay = 5.seconds,
        backoffFactor = 1.5f
      ),
      releasedStatusCheck = StatusCheck(
        giveUpAfter = 2.hours,
        initialRetryDelay = 2.minutes,
        backoffFactor = 1.5f
      )
    )
  }
}
