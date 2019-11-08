package nevam.nexus

import nevam.extensions.minutes
import nevam.extensions.seconds
import java.time.Duration

data class NexusConfig(
  val statusCheck: StatusCheck
) {
  data class StatusCheck(
    val giveUpAfter: Duration,
    val initialRetryDelay: Duration
  )

  companion object {
    val DEFAULT = NexusConfig(
        statusCheck = StatusCheck(
            giveUpAfter = 10.minutes,
            initialRetryDelay = 5.seconds
        )
    )
  }
}
