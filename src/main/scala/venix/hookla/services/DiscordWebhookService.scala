package venix.hookla.services

import com.google.inject.Inject
import io.getquill.{CamelCase, PostgresAsyncContext}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import venix.hookla.models.{DiscordWebhook, EmbedOptions}

class DiscordWebhookService @Inject()(
  dbContext: PostgresAsyncContext[CamelCase],
  embedOptionsService: EmbedOptionsService
)(
  implicit executionContext: ExecutionContext
) {
  import dbContext._

  private val discordWebhooks = quote(querySchema[DiscordWebhook]("discord_webhooks"))

  def getById(id: UUID): Future[Option[DiscordWebhook]] =
    dbContext.run(discordWebhooks.filter(_.id == lift(id))).map(_.headOption)
}