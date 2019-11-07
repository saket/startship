package nevam

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import nevam.extensions.seconds
import nevam.nexus.StatusCheckState.WillRetry
import org.junit.Test

class FooTest {

  @Test fun foo() {
    val statusChecks = Observable.just(WillRetry).repeat(5)

    statusChecks
        .ofType<WillRetry>()
        .scanWith({ 5.seconds }, { prev, curr -> prev + 5.seconds })
        .blockingForEach {
          println("Delay: ${it.seconds}s")
        }
  }
}
