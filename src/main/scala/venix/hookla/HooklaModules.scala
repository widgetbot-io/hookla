package venix.hookla

import io.circe.config._
import io.circe.generic.auto._
import io.getquill.{CamelCase, PostgresAsyncContext}
import scala.concurrent.ExecutionContext
import venix.hookla.controllers._
import venix.hookla.handlers.MainHandler
import venix.hookla.services._

trait HooklaModules {
  import com.softwaremill.macwire._

  lazy val config: HooklaConfig =
    parser
      .decode[HooklaConfig]()
      .fold(
        errors => throw errors.fillInStackTrace(),
        identity
      )

  implicit lazy val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val postgresAsyncContext: PostgresAsyncContext[CamelCase] =
    new PostgresAsyncContext(CamelCase, "postgres")

  // Services
  lazy val discordMessageService   = wire[DiscordMessageService]
  lazy val discordWebhookService   = wire[DiscordWebhookService]
  lazy val embedOptionsService     = wire[EmbedOptionsService]
  lazy val providerSettingsService = wire[ProviderSettingsService]

  // Handlers
  lazy val mainHandler = wire[MainHandler]

  // Controllers
  lazy val webhookController = wire[WebhookController]
}
