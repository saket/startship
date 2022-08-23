package nevam.network

import com.github.ajalt.clikt.output.TermUi.echo
import com.slack.eithernet.ApiResultCallAdapterFactory
import com.slack.eithernet.ApiResultConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger
import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Duration

class NetworkModule(debugMode: Boolean) {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  val okHttpBuilder = OkHttpClient.Builder()
    .addInterceptor(XmlToJsonBodyInterceptor)
    .connectTimeout(Duration.ofSeconds(30))
    .readTimeout(Duration.ofSeconds(30))
    .apply {
      if (debugMode) {
        val cliktLogger = object : Logger {
          override fun log(message: String) {
            echo("OkHttp: $message")
          }
        }
        addInterceptor(HttpLoggingInterceptor(cliktLogger).apply { level = BODY })
      }
    }

  val retrofitBuilder: Builder = Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addConverterFactory(ApiResultConverterFactory)
    .addCallAdapterFactory(ApiResultCallAdapterFactory)
    .client(okHttpBuilder.build())
    .validateEagerly(true)
}
