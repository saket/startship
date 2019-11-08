package nevam

import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS

class FooTest {

  @Test fun foo() {
    val foo = """
      |Gave up after trying for 10 minutes. It usually doesn't take this long,
      |and is probably an indication that Nexus is unavailable. Try again after some time?
    """.trimIndent()

    assertThat(foo).isEqualTo("")
  }
}
