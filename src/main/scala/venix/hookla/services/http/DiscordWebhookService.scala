package venix.hookla.services.http

import io.circe.Codec
import io.circe.generic.semiauto._
import sttp.client3.UriContext
import venix.hookla.{HooklaConfig, Result}
import venix.hookla.services.core.{HTTPService, Options}
import zio.ZLayer

trait DiscordWebhookService {

}

private class DiscordWebhookServiceImpl(private val http: HTTPService, private val config: HooklaConfig) extends DiscordWebhookService {
  def execute(id: String): Result[Option[DiscordUser]] = http.post[Option[DiscordUser]](uri"https://canary.discord.com/api/webhooks/689887952268165178/J6GACLSgtVdOKO_tP3CWVmy_PV3_3A6T8Pc2lL1b0ZUHCviVQlhk31ElB7_vJA7w_rIK", Options().addHeader("Authorization", s"Bot ${config.discord.token}"))
}

object DiscordWebhookService {
  private type In = HTTPService with HooklaConfig
  private def create(httpService: HTTPService, c: HooklaConfig) = new DiscordWebhookServiceImpl(httpService, c)

  val live: ZLayer[In, Throwable, DiscordWebhookService] = ZLayer.fromFunction(create _)
}
