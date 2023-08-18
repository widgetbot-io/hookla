package venix.hookla.services.http

import io.circe.Codec
import io.circe.generic.semiauto._
import sttp.client3.UriContext
import venix.hookla.{HooklaConfig, Result}
import venix.hookla.services.core.{HTTPService, Options}
import zio.ZLayer

case class DiscordUser(
    id: String,
    username: String,
    globalName: Option[String],
    avatar: String
) {
  def getName = globalName.getOrElse(username)

  def toEntity: venix.hookla.entities.DiscordUser = venix.hookla.entities.DiscordUser(
    id,
    getName,
    avatar
  )
}

object DiscordUser {
  implicit val codec: Codec[DiscordUser] = deriveCodec
}

trait DiscordUserService {
  def get(id: String): Result[Option[DiscordUser]]
}

private class DiscordUserServiceImpl(private val http: HTTPService, private val config: HooklaConfig) extends DiscordUserService {
  def get(id: String): Result[Option[DiscordUser]] = http.get[Option[DiscordUser]](uri"https://discord.com/api/users/$id", Options().addHeader("Authorization", s"Bot ${config.discord.token}"))
}

object DiscordUserService {
  private type In = HTTPService with HooklaConfig
  private def create(httpService: HTTPService, c: HooklaConfig) = new DiscordUserServiceImpl(httpService, c)

  val live: ZLayer[In, Throwable, DiscordUserService] = ZLayer.fromFunction(create _)
}
