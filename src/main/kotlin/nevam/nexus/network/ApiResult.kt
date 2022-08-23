package nevam.nexus.network

import com.slack.eithernet.ApiResult

//sealed class ApiResult<out T : Any> {
//  data class Success<T : Any>(val response: T?) : ApiResult<T>()
//
//  data class Failure(val error: Throwable?, val type: Type) : ApiResult<Nothing>() {
//    enum class Type {
//      Server,
//      Network,
//      UserAuth,
//      Unknown,
//      NotFound
//    }
//  }
//}

enum class FailureType {
  Server,
  Network,
  UserAuth,
  Unknown,
  NotFound
}

val ApiResult.Failure<*>.type: FailureType
  get() {
    return when (this) {
      is ApiResult.Failure.NetworkFailure -> FailureType.Network
      is ApiResult.Failure.UnknownFailure -> FailureType.Unknown
      is ApiResult.Failure.ApiFailure -> FailureType.Unknown
      is ApiResult.Failure.HttpFailure -> {
        when (this.code) {
          401 -> FailureType.UserAuth
          404 -> FailureType.NotFound
          else -> FailureType.Server
        }
      }
    }
  }
