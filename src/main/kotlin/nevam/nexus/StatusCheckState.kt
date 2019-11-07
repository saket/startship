package nevam.nexus

import java.time.Duration

sealed class StatusCheckState {
  object Checking : StatusCheckState()
  object WillRetry : StatusCheckState()
  data class RetryingIn(val secondsRemaining: Long) : StatusCheckState()
  object Done : StatusCheckState()
  data class GaveUp(val after: Duration) : StatusCheckState()
}
