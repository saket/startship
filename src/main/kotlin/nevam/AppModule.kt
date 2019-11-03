package nevam

import com.github.ajalt.clikt.output.TermUi.echo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nevam.nexus.NexusModule
import nevam.nexus.RealNexusRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import okhttp3.logging.HttpLoggingInterceptor.Logger
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.moshi.MoshiConverterFactory

class AppModule(
  user: NexusUser,
  debugMode: Boolean
) {
  private val networkModule = NetworkModule(debugMode)

  private val nexusModule = NexusModule(
      networkModule = networkModule,
      repositoryUrl = "https://oss.sonatype.org",
      user = user
  )

  val nexusRepository = RealNexusRepository(
      api = nexusModule.nexusApi
  )
}

class NetworkModule(debugMode: Boolean) {
  private val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

  val okHttpBuilder = OkHttpClient.Builder()
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

  val retrofitBuilder: Builder = Retrofit
      .Builder()
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .client(okHttpBuilder.build())
}
