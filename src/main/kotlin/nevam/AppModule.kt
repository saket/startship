package nevam

import kotlinx.coroutines.Dispatchers
import nevam.network.NetworkModule
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus

class AppModule(
  user: NexusUser,
  debugMode: Boolean,
  hostPrefix: String = "",
  val poms: List<Pom>
) {
  internal val nexusModule = NexusModule(
    networkModule = NetworkModule(debugMode),
    repositoryUrl = repositoryUrl(hostPrefix),
    user = user
  )

  val nexus = RealNexus(
    api = nexusModule.nexusApi,
    debugMode = debugMode,
    config = NexusConfig.DEFAULT,
    ioDispatcher = Dispatchers.IO
  )

  private companion object {
    private fun repositoryUrl(hostPrefix: String): String {
      val actualPrefix = if (hostPrefix.isEmpty()) "" else "$hostPrefix."
      return "https://${actualPrefix}oss.sonatype.org"
    }
  }
}
