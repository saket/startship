package nevam.nexus

import com.google.common.truth.Truth.assertThat
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import nevam.FAKE
import nevam.extensions.advanceTimeBy
import nevam.extensions.minutes
import nevam.extensions.second
import nevam.extensions.seconds
import nevam.isInstanceOf
import nevam.nexus.StatusCheckState.Checking
import nevam.nexus.StatusCheckState.Done
import nevam.nexus.StatusCheckState.GaveUp
import nevam.nexus.StatusCheckState.RetryingIn
import nevam.nexus.StatusCheckState.WillRetry
import org.junit.Test

class RealNexusTest {

  private val api = FakeNexusApi()
  private val testScheduler = TestScheduler()
  private val config = NexusConfig.DEFAULT

  private val nexus = RealNexus(
      api = api,
      debugMode = false,
      config = config
  )

  init {
    RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
  }

  @Test fun `poll status with gradual back-off`() {
    val repositoryId = "nicolascage"

    val statusValues = nexus
        .pollUntilClosed(repositoryId)
        .test()
        .values()
    assertThat(statusValues.last()).isInstanceOf<Checking>()

    api.repository.onNext(
        StagingProfileRepository.FAKE.copy(
            id = repositoryId,
            type = "open",
            isTransitioning = true
        )
    )
    assertThat(statusValues.last()).isInstanceOf<WillRetry>()

    testScheduler.advanceTimeBy(config.statusCheck.initialRetryDelay)
    assertThat(statusValues).containsExactly(
        Checking,
        WillRetry,
        RetryingIn(secondsRemaining = 5),
        RetryingIn(secondsRemaining = 4),
        RetryingIn(secondsRemaining = 3),
        RetryingIn(secondsRemaining = 2),
        RetryingIn(secondsRemaining = 1)
    ).inOrder()

    testScheduler.advanceTimeBy(config.statusCheck.initialRetryDelay + 4.seconds)
    assertThat(statusValues).containsExactly(
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

  @Test fun `stop polling for status after expiry time`() {
    val repositoryId = "nicolascage"
    api.repository.onNext(
        StagingProfileRepository.FAKE.copy(
            id = repositoryId,
            type = "open",
            isTransitioning = true
        )
    )

    val statusValues = nexus
        .pollUntilClosed(repositoryId)
        .test()
        .values()

    assertThat(statusValues).containsExactly(Checking, WillRetry)

    testScheduler.advanceTimeBy(config.statusCheck.giveUpAfter)
    assertThat(statusValues.last()).isInstanceOf<GaveUp>()
  }

  @Test fun `stop polling for status once repository is closed`() {
    val repositoryId = "nicolascage"
    api.repository.onNext(
        StagingProfileRepository.FAKE.copy(
            id = repositoryId,
            type = "open",
            isTransitioning = true
        )
    )

    val statusValues = nexus
        .pollUntilClosed(repositoryId)
        .test()
        .values()

    assertThat(statusValues).containsExactly(Checking, WillRetry)

    api.repository.onNext(
        StagingProfileRepository.FAKE.copy(
            id = repositoryId,
            type = "closed",
            isTransitioning = false
        )
    )
    testScheduler.advanceTimeBy(config.statusCheck.initialRetryDelay + 1.second)
    assertThat(statusValues.last()).isInstanceOf<Done>()
  }
}
