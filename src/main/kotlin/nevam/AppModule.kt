package nevam

import com.github.ajalt.clikt.output.defaultCliktConsole
import io.reactivex.plugins.RxJavaPlugins
import nevam.clikt.UserInput
import nevam.network.NetworkModule
import nevam.nexus.NexusConfig
import nevam.nexus.NexusModule
import nevam.nexus.RealNexus

class AppModule(
  user: NexusUser,
  debugMode: Boolean,
  pom: Pom
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

  private val nexusRepository = RealNexus(
      api = nexusModule.nexusApi,
      debugMode = debugMode,
      config = NexusConfig.DEFAULT
  )

  val nexusCommand = NexusCommand(
      nexus = nexusRepository,
      input = UserInput(defaultCliktConsole()),
      pom = pom
  )
}

