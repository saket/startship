package nevam

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import nevam.network.NetworkModule
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus

class AppModule(user: NexusUser, debugMode: Boolean, val pom: Pom) {
  init {
    RxJavaPlugins.setErrorHandler { /* Ignored exceptions. */ }
  }

  private val nexusModule = NexusModule(
      networkModule = NetworkModule(debugMode),
      repositoryUrl = "https://oss.sonatype.org",
      user = user
  )

  val nexusRepository = RealNexus(
      api = nexusModule.nexusApi,
      debugMode = debugMode,
      config = NexusConfig.DEFAULT,
      scheduler = Schedulers.single()
  )
}
