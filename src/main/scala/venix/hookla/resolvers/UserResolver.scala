package venix.hookla.resolvers

import venix.hookla.RequestError
import venix.hookla.entities.{DiscordUser, Team, User}
import venix.hookla.services.db.{IUserService, UserService}
import venix.hookla.types.UserId
import zio.{ZIO, ZLayer}
import venix.hookla.RequestError._

import java.util.UUID

trait IUserResolver {
  def me: Result[User]
}

class UserResolver(
    private val userService: IUserService
) extends IUserResolver {

  def me: Result[User] = userService
    .getById(UserId(UUID.fromString("181ec752-2784-4fe7-a031-d13533238f63")))
    .mapBoth(
      _ => UnknownError,
      _.fold(sys.error("boom!")) { user =>
        User(
          user.id.unwrap
//          resolveDiscordUser(user.discordId),
//          resolveTeams(user.id)
        )
      }
    )

  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]] = ZIO.succeed(???)
  def resolveTeams(userId: UserId): Result[List[Team]]                   = ZIO.succeed(???)
}

object UserResolver {
  private type In = IUserService
  private def create(userService: IUserService) = new UserResolver(userService)

  val live: ZLayer[In, Throwable, IUserResolver] = ZLayer.fromFunction(create _)
}
