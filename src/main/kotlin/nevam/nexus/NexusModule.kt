package nevam.nexus

import nevam.network.NetworkModule
import nevam.NexusUser
import nevam.nexus.network.NexusApi
import okhttp3.Credentials
import okhttp3.Interceptor

class NexusModule(
  networkModule: NetworkModule,
  repositoryUrl: String,
  user: NexusUser
) {
  private val authInterceptor = Interceptor { chain ->
    chain.proceed(
        chain.request().newBuilder()
            .addHeader("Authorization", Credentials.basic(user.username, user.password))
            .build()
    )
  }

  private val contentTypeInterceptor = Interceptor { chain ->
    chain.proceed(
        chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()
    )
  }

  private val okHttpClient = networkModule.okHttpBuilder
      .addNetworkInterceptor(authInterceptor)
      .addNetworkInterceptor(contentTypeInterceptor)
      .build()

  val nexusApi = networkModule.retrofitBuilder
      .baseUrl(repositoryUrl)
      .client(okHttpClient)
      .build()
      .create(NexusApi::class.java)
}
