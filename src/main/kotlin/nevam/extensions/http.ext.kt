package nevam.extensions

import io.reactivex.Completable
import io.reactivex.Single
import nevam.nexus.network.ApiResult
import nevam.nexus.network.ApiResult.Failure
import nevam.nexus.network.ApiResult.Failure.Type.Network
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
    when (e) {
      is IOException -> Failure(e, Network)
      is HttpException -> when {
        e.code() == 401 -> Failure(e, UserAuth)
        else -> Failure(e, Server)
      }
      else -> Failure(e, Unknown)
    }
  }
}

fun <T : Any> Single<T>.mapToResult(): Single<ApiResult<T>> {
  return map<ApiResult<T>> { response -> Success(response) }
      .onErrorReturn { e ->
        when (e) {
          is IOException -> Failure(e, Network)
          is HttpException -> when {
            e.code() == 401 -> Failure(e, UserAuth)
            else -> Failure(e, Server)
          }
          else -> Failure(e, Unknown)
        }
      }
}

fun Completable.mapToResult(): Single<ApiResult<Nothing>> {
  return andThen(Single.just<ApiResult<Nothing>>(Success(null)))
      .onErrorReturn { e ->
        when (e) {
          is IOException -> Failure(e, Network)
          is HttpException -> when {
            e.code() == 401 -> Failure(e, UserAuth)
            else -> Failure(e, Server)
          }
          else -> Failure(e, Unknown)
        }
      }
}
