package venix.hookla.resolvers

import venix.hookla.RequestError
import venix.hookla.entities.{DiscordUser, Team, User}
import venix.hookla.services.db.{IUserService, UserService}
import venix.hookla.types.UserId
import zio.{ZIO, ZLayer}
import venix.hookla.RequestError._
import venix.hookla.services.http.IDiscordUserService

import java.util.UUID

trait IUserResolver {
  def me: Result[User]
}

class UserResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService
) extends IUserResolver {

  def me: Result[User] = userService
    .getById(UserId(UUID.fromString("181ec752-2784-4fe7-a031-d13533238f63")))
    .mapBoth(
      _ => UnknownError,
      _.fold(sys.error("boom!")) { user =>
        User(
          user.id.unwrap,
          resolveDiscordUser(user.discordId),
          resolveTeams(user.id)
        )
      }
    )

  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]] = discordUserService.get(discordId).map(_.map(_.toEntity))
  def resolveTeams(userId: UserId): Result[List[Team]]                   = ZIO.succeed(???)
}

object UserResolver {
  private type In = IUserService with IDiscordUserService
  private def create(userService: IUserService, discordUserService: IDiscordUserService) = new UserResolver(discordUserService, userService)

  val live: ZLayer[In, Throwable, IUserResolver] = ZLayer.fromFunction(create _)
}
