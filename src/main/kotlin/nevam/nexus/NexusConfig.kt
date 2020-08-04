package nevam.nexus

import nevam.util.hours
import nevam.util.minutes
import nevam.util.seconds
import java.time.Duration

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
