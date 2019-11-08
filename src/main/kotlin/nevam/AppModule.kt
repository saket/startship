package nevam

import com.github.ajalt.clikt.output.TermUi.echo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.plugins.RxJavaPlugins
import nevam.extensions.seconds
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import okhttp3.logging.HttpLoggingInterceptor.Logger
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class AppModule(
  user: NexusUser,
  debugMode: Boolean
) {
  init {
    RxJavaPlugins.setErrorHandler { /* Ignored exceptions. */ }
  }

  private val networkModule = NetworkModule(debugMode)

  private val nexusModule = NexusModule(
      networkModule = networkModule,
      repositoryUrl = "https://oss.sonatype.org",
      user = user
  )

  val nexusRepository = RealNexus(
      api = nexusModule.nexusApi,
      debugMode = debugMode,
      config = NexusConfig.DEFAULT
  )
}

class NetworkModule(debugMode: Boolean) {
  private val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

  val okHttpBuilder = OkHttpClient.Builder()
      .connectTimeout(30.seconds)
      .readTimeout(30.seconds)
      .apply {
        if (debugMode) {
          val cliktLogger = object : Logger {
            override fun log(message: String) {
              echo("OkHttp: $message")
            }
          }
          addInterceptor(HttpLoggingInterceptor(cliktLogger).apply { level = Level.BODY })
        }
      }

  val retrofitBuilder: Retrofit.Builder = Retrofit
      .Builder()
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(okHttpBuilder.build())
      .validateEagerly(true)
}
