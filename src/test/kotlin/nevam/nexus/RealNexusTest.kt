@file:OptIn(ExperimentalCoroutinesApi::class)

package nevam.nexus

import app.cash.turbine.testIn
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import nevam.nexus.StatusCheckState.Checking
import nevam.nexus.StatusCheckState.Done
import nevam.nexus.StatusCheckState.GaveUp
import nevam.nexus.StatusCheckState.RetryingIn
import nevam.nexus.StatusCheckState.WillRetry
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RealNexusTest {
  private val api = FakeNexusApi()
  private val config = NexusConfig.DEFAULT

  private fun TestScope.nexus() = RealNexus(
    api = api,
    debugMode = false,
    config = config,
    ioDispatcher = testScheduler
  )

  @Test fun `poll status with gradual back-off`() = runTest {
    val repositoryId = "nicolascage"

    val statusValues = nexus()
      .pollUntilClosed(repositoryId)
      .testIn(this)
    assertThat(statusValues.awaitItem()).isInstanceOf<Checking>()

    api.repository.add(
      FAKE_STAGING_REPO.copy(
        id = repositoryId,
        type = "open",
        isTransitioning = true
      )
    )
    testScheduler.runCurrent()
    assertThat(statusValues.awaitItem()).isInstanceOf<WillRetry>()

    testScheduler.advanceTimeBy(config.closedStatusCheck.initialRetryDelay)
    statusValues.run {
      assertThat(awaitItem()).isEqualTo(Checking)
      assertThat(awaitItem()).isEqualTo(WillRetry)
      assertThat(awaitItem()).isEqualTo(RetryingIn(secondsRemaining = 5))
      assertThat(awaitItem()).isEqualTo(RetryingIn(secondsRemaining = 4))
      assertThat(awaitItem()).isEqualTo(RetryingIn(secondsRemaining = 3))
      assertThat(awaitItem()).isEqualTo(RetryingIn(secondsRemaining = 2))
      assertThat(awaitItem()).isEqualTo(RetryingIn(secondsRemaining = 1))
    }

    testScheduler.advanceTimeBy(config.closedStatusCheck.initialRetryDelay.plus(4.seconds))
    assertThat(statusValues.cancelAndConsumeRemainingEvents()).containsExactly(
      Checking,
      WillRetry,
      RetryingIn(secondsRemaining = 5),
      RetryingIn(secondsRemaining = 4),
      RetryingIn(secondsRemaining = 3),
      RetryingIn(secondsRemaining = 2),
      RetryingIn(secondsRemaining = 1),
      Checking,
      WillRetry,
      RetryingIn(secondsRemaining = 7),
      RetryingIn(secondsRemaining = 6),
      RetryingIn(secondsRemaining = 5),
      RetryingIn(secondsRemaining = 4),
      RetryingIn(secondsRemaining = 3),
      RetryingIn(secondsRemaining = 2),
      RetryingIn(secondsRemaining = 1),
      Checking,
      WillRetry
    ).inOrder()
  }

  @Test fun `stop polling for status after expiry time`() = runTest {
    val repositoryId = "nicolascage"
    api.repository.add(
      FAKE_STAGING_REPO.copy(
        id = repositoryId,
        type = "open",
        isTransitioning = true
      )
    )

    val statusValues = nexus()
      .pollUntilClosed(repositoryId)
      .testIn(this)

    testScheduler.runCurrent()
    assertThat(statusValues.awaitItem()).isEqualTo(Checking)
    assertThat(statusValues.awaitItem()).isEqualTo(WillRetry)

    testScheduler.advanceTimeBy(config.closedStatusCheck.giveUpAfter)
    assertThat(statusValues.expectMostRecentItem()).isInstanceOf<GaveUp>()
  }

  @Test fun `stop polling for status once repository is closed`() = runTest {
    val repositoryId = "nicolascage"
    api.repository.add(
      FAKE_STAGING_REPO.copy(
        id = repositoryId,
        type = "open",
        isTransitioning = true
      )
    )

    val statusValues = nexus()
      .pollUntilClosed(repositoryId)
      .testIn(this)

    testScheduler.runCurrent()
    assertThat(statusValues.awaitItem()).isEqualTo(Checking)
    assertThat(statusValues.awaitItem()).isEqualTo(WillRetry)

    api.repository.add(
      FAKE_STAGING_REPO.copy(
        id = repositoryId,
        type = "closed",
        isTransitioning = false
      )
    )
    testScheduler.advanceTimeBy(config.closedStatusCheck.initialRetryDelay + 1.seconds)
    assertThat(statusValues.expectMostRecentItem()).isInstanceOf<Done>()
  }

  @Test fun `parse relative timestamp`() {
    val nowTime = Instant.ofEpochMilli(1596505477212) // Tue Aug 04 01:44:37 UTC 2020

    with(FAKE_STAGING_REPO.copy(updatedAtString = "Tue Aug 04 01:44:00 UTC 2020")) {
      val timestamp = timestampRelativeToNow(clock = Clock.fixed(nowTime, UTC))
      assertThat(timestamp).isEqualTo("37s ago")
    }

    with(FAKE_STAGING_REPO.copy(updatedAtString = "Tue Aug 04 01:17:19 UTC 2020")) {
      val timestamp = timestampRelativeToNow(clock = Clock.fixed(nowTime, UTC))
      assertThat(timestamp).isEqualTo("27m 18s ago")
    }

    with(FAKE_STAGING_REPO.copy(updatedAtString = "Mon Aug 03 14:17:19 UTC 2020")) {
      val timestamp = timestampRelativeToNow(clock = Clock.fixed(nowTime, UTC))
      assertThat(timestamp).isEqualTo("11h 27m ago")
    }

    with(FAKE_STAGING_REPO.copy(updatedAtString = "Sun Aug 02 01:17:19 UTC 2020")) {
      val timestamp = timestampRelativeToNow(clock = Clock.fixed(nowTime, UTC))
      assertThat(timestamp).isEqualTo("Sun Aug 02 01:17:19 UTC 2020")
    }
  }

  companion object {
    val FAKE_STAGING_REPO = StagingProfileRepository(
      id = "cagenicolas_1206",
      type = "closed",
      isTransitioning = false,
      updatedAtString = "Sometime",
      profileId = "9000",
      profileName = "cage.nicolas"
    )
  }
}

private inline fun <reified T> Subject.isInstanceOf() {
  return isInstanceOf(T::class.java)
}

private fun TestCoroutineScheduler.advanceTimeBy(delayTime: Duration) {
  advanceTimeBy(delayTime.inWholeMilliseconds)
}
