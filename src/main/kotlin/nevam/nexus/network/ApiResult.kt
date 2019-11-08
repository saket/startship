package nevam.nexus.network

sealed class ApiResult<out T : Any> {
  data class Success<T : Any>(val response: T?) : ApiResult<T>()
  data class Failure(val error: Throwable, val type: Type) : ApiResult<Nothing>() {
    enum class Type {
      Server,
      Network,
      UserAuth,
      Unknown
    }
  }
}
