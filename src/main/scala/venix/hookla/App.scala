package venix.hookla

import com.google.inject.Guice
import com.twitter.finagle.{Http, Service, http}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.{Application, Bootstrap}
import org.flywaydb.core.Flyway
import venix.hookla.controllers.WebhookController
import io.circe.generic.auto._
import io.finch.circe._
import venix.hookla.modules.{ActorModule, AkkaModule, MainModule}

object App extends TwitterServer {
  import net.codingwell.scalaguice.InjectorExtensions._
  import venix.hookla.util.ExceptionEncoder._

  protected val injector = Guice.createInjector(new MainModule, new AkkaModule)
  protected val actorInjector = injector.createChildInjector(new ActorModule(injector))
  private val config = injector.instance[HooklaConfig]

  private val webhookController = actorInjector.instance[WebhookController]

  val service: Service[http.Request, http.Response] = Bootstrap
    .serve[Application.Json](webhookController.endpoints)
    .toService


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

    val server = Http.server
      .serve(s":${config.app.port}", service)

    onExit { server.close() }
    Await.ready(adminHttpServer)
  }
}
