package venix.hookla

import io.circe.config._
import io.circe.generic.auto._
import io.getquill.{PostgresAsyncContext, SnakeCase}

import scala.concurrent.ExecutionContext
import venix.hookla.controllers._
import venix.hookla.handlers.MainHandler
import venix.hookla.services._

trait HooklaModules {
  import com.softwaremill.macwire._

  val config: HooklaConfig = parser.decode[HooklaConfig]().fold(errors => throw errors.fillInStackTrace(), identity)

  implicit val executionContext: ExecutionContext           = scala.concurrent.ExecutionContext.Implicits.global
  val postgresAsyncContext: PostgresAsyncContext[SnakeCase] = new PostgresAsyncContext(SnakeCase, "postgres")

  // Services
  lazy val discordMessageService: DiscordMessageService     = wire[DiscordMessageService]
  lazy val discordWebhookService: DiscordWebhookService     = wire[DiscordWebhookService]
  lazy val embedOptionsService: EmbedOptionsService         = wire[EmbedOptionsService]
  lazy val providerSettingsService: ProviderSettingsService = wire[ProviderSettingsService]

  // Handlers
  lazy val mainHandler: MainHandler = wire[MainHandler]

  // Controllers
  lazy val webhookController: WebhookController = wire[WebhookController]
}
