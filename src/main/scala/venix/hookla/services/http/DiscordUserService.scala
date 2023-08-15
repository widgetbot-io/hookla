package venix.hookla.services.http

import io.circe.Codec
import io.circe.generic.semiauto._
import sttp.client3.UriContext
import venix.hookla.{HooklaConfig, Result}
import venix.hookla.services.core.{IHTTPService, Options}
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

trait IDiscordUserService {
  def get(id: String): Result[Option[DiscordUser]]
}

class DiscordUserService(private val http: IHTTPService, private val config: HooklaConfig) extends IDiscordUserService {
  def get(id: String): Result[Option[DiscordUser]] = http.get[Option[DiscordUser]](uri"https://discord.com/api/users/$id", Options().addHeader("Authorization", s"Bot ${config.discord.token}"))
}

object DiscordUserService {
  private type In = IHTTPService with HooklaConfig

  private def create(httpService: IHTTPService, c: HooklaConfig) = new DiscordUserService(httpService, c)

  val live: ZLayer[In, Throwable, IDiscordUserService] = ZLayer.fromFunction(create _)
}
