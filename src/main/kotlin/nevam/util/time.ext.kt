@file:Suppress("ReactiveStreamsUnusedPublisher")

package nevam.util

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import java.time.Duration
import java.util.concurrent.TimeUnit.MILLISECONDS

val Int.minutes: Duration
  get() = Duration.ofMinutes(toLong())

val Int.hours: Duration
  get() = Duration.ofHours(toLong())

val Int.hour: Duration
  get() = Duration.ofHours(toLong())

val Int.seconds: Duration
  get() = Duration.ofSeconds(toLong())

val Long.seconds: Duration
  get() = Duration.ofSeconds(this)

val Int.second: Duration
  get() = Duration.ofSeconds(toLong())

object Observables {
  fun timer(delay: Duration, scheduler: Scheduler) =
    Observable.timer(delay.toMillis(), MILLISECONDS, scheduler)
        .map { delay }!!

  fun interval(period: Duration, initial: Duration = period, scheduler: Scheduler) =
    Observable
        .interval(initial.toMillis(), period.toMillis(), MILLISECONDS, scheduler)
        .map { Duration.ofMillis(it * period.toMillis()) }!!
}

fun <T> Observable<T>.delay(period: Duration, scheduler: Scheduler) =
  delay(period.toMillis(), MILLISECONDS, scheduler)!!

fun TestScheduler.advanceTimeBy(delayTime: Duration) {
  advanceTimeBy(delayTime.toMillis(), MILLISECONDS)
}
