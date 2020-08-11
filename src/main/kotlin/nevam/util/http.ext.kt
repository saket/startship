package nevam.util

import io.reactivex.Single
import nevam.nexus.network.ApiResult
import nevam.nexus.network.ApiResult.Failure
import nevam.nexus.network.ApiResult.Failure.Type.Network
import nevam.nexus.network.ApiResult.Failure.Type.NotFound
import nevam.nexus.network.ApiResult.Failure.Type.Server
import nevam.nexus.network.ApiResult.Failure.Type.Unknown
import nevam.nexus.network.ApiResult.Failure.Type.UserAuth
import nevam.nexus.network.ApiResult.Success
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

fun <T : Any> Call<T>.executeAsResult(): ApiResult<T> {
  return try {
    Success(execute().body())
  } catch (e: Throwable) {
    Failure(e, e.type())
  }
}

fun <T : Any> Single<T>.mapToResult(): Single<ApiResult<T>> {
  return map<ApiResult<T>> { response -> Success(response) }
      .onErrorReturn { e -> Failure(e, e.type()) }
}

private fun Throwable.type(): Failure.Type {
  return when (this) {
    is IOException -> Network
    is HttpException -> when {
      code() == 401 -> UserAuth
      code() == 404 -> NotFound
      else -> Server
    }
    else -> Unknown
  }
}
