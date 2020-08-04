package nevam

import io.reactivex.plugins.RxJavaPlugins
import nevam.network.NetworkModule
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus
import nevam.nexus.network.MOCK_NEXUS_CONFIG
import nevam.nexus.network.MockNexusApi

class AppModule(
  user: NexusUser,
  debugMode: Boolean,
  val pom: Pom
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

