package venix.hookla

import com.google.inject.Guice
import com.twitter.server.TwitterServer
import org.flywaydb.core.Flyway

object App extends TwitterServer {
  import net.codingwell.scalaguice.InjectorExtensions._

  protected val injector = Guice.createInjector(new MainModule)
  private val config = injector.instance[HooklaConfig]

  private def migrateDb(): Unit = {
    val flyway = new Flyway()
    flyway.setDataSource(
      config.flywayConfig.url,
      config.flywayConfig.user,
      config.flywayConfig.password
    )
    flyway.migrate()
  }

  def main(): Unit = {
    migrateDb()
  }
}
