package venix.hookla

import com.twitter.finagle.{Http, Service, http}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.finch.{Application, Bootstrap}
import org.flywaydb.core.Flyway
import venix.hookla.controllers.WebhookController
import io.circe.generic.auto._
import io.finch.circe._

object App extends TwitterServer with HooklaModules {
  import venix.hookla.util.ExceptionEncoder._

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

    val server = Http.server.serve(s":${config.app.port}", service)
    logger.info(s"Server started on port ${config.app.port}")

    onExit(server.close())
    Await.ready(adminHttpServer)
  }
}
