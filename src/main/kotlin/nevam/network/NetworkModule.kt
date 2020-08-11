package nevam.network

import com.github.ajalt.clikt.output.TermUi.echo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nevam.util.seconds
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger
import retrofit2.Retrofit.Builder
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkModule(debugMode: Boolean) {
  private val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

  val okHttpBuilder = OkHttpClient.Builder()
      .addInterceptor(XmlToJsonBodyInterceptor)
      .connectTimeout(30.seconds)
      .readTimeout(30.seconds)
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
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(okHttpBuilder.build())
      .validateEagerly(true)
}
