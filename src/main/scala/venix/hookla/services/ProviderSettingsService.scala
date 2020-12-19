package venix.hookla.services

import com.google.inject.Inject
import io.getquill.{CamelCase, PostgresAsyncContext}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import venix.hookla.models.{DiscordWebhook, EmbedOptions, ProviderSettings}

class ProviderSettingsService @Inject()(
  dbContext: PostgresAsyncContext[CamelCase],
  embedOptionsService: EmbedOptionsService
)(
  implicit executionContext: ExecutionContext
) {

  import dbContext._

  private val providerSettings = quote(querySchema[ProviderSettings]("provider_settings"))

  def getById(id: UUID): Future[Option[ProviderSettings]] =
    dbContext.run(providerSettings.filter(_.id == lift(id))).map(_.headOption)

  def getByToken(token: String): Future[Option[ProviderSettings]] =
    dbContext.run(providerSettings.filter(_.token == lift(token))).map(_.headOption)

  def getOptionsForProvider(providerSettings: ProviderSettings): Future[Option[EmbedOptions]] =
    providerSettings.optionsId.fold[Future[Option[EmbedOptions]]](Future.successful(None)) { id =>
      embedOptionsService.getById(id)
    }
}
