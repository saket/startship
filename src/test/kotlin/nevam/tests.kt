package nevam

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
