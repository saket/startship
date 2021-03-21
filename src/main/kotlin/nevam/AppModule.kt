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
      repositoryUrl = repositoryUrl(hostPrefix),
      user = user
  )

  val nexus = RealNexus(
      api = nexusModule.nexusApi,
      debugMode = debugMode,
      config = NexusConfig.DEFAULT,
      scheduler = Schedulers.single()
  )

  private companion object {
    private fun repositoryUrl(hostPrefix: String): String {
      val actualPrefix = if (hostPrefix.isEmpty())
        ""
      else
        "$hostPrefix."
      return "https://${actualPrefix}oss.sonatype.org"
    }
  }
}
