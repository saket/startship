package nevam

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import nevam.network.NetworkModule
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus

class AppModule(user: NexusUser, debugMode: Boolean, val poms: List<Pom>, hostPrefix: String = "") {

  init {
    RxJavaPlugins.setErrorHandler { /* Ignored exceptions. */ }
  }

  internal val nexusModule = NexusModule(
      networkModule = NetworkModule(debugMode),
      repositoryUrl = "https://${if (hostPrefix.isEmpty()) "" else "$hostPrefix."}oss.sonatype.org",
      user = user
  )

  val nexus = RealNexus(
      api = nexusModule.nexusApi,
      debugMode = debugMode,
      config = NexusConfig.DEFAULT,
      scheduler = Schedulers.single()
  )
}
