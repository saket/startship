package nevam.util

import java.io.PrintWriter
import java.io.StringWriter

fun Throwable.stacktraceToString(): String {
  val stacktraceString = StringWriter()
  printStackTrace(PrintWriter(stacktraceString))
  return stacktraceString.toString()
}
