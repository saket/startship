package nevam.util

import io.reactivex.Observable

fun Observables.fibonacci(): Observable<Long> {
  return Observable.create<Long> { emitter ->
    var a = 0L; var b = 1L; var c = 1L
    while (!emitter.isDisposed) {
      emitter.onNext(c)
      c = a + b
      a = b
      b = c
    }
  }
}
