package nevam

import com.google.common.truth.Subject

inline fun <reified T> Subject.isInstanceOf() {
  return isInstanceOf(T::class.java)
}

fun expectError(action: () -> Unit): Throwable {
  var error: Throwable? = null
  try {
    action()
  } catch (e: Throwable) {
    error = e
  }
  checkNotNull(error) { "No error detected" }
  return error
}
