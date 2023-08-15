package venix.hookla.resolvers

import venix.hookla.RequestError._
import venix.hookla.{Result, Task}
import venix.hookla.entities.{DiscordUser, Team, User}
import venix.hookla.http.Auth
import venix.hookla.services.db.{ITeamService, IUserService}
import venix.hookla.services.http.IDiscordUserService
import venix.hookla.types.UserId
import zio.{ZIO, ZLayer}

import java.util.UUID

trait IUserResolver {
  // Queries
  def me: Task[User]

  // Resolvers
  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]]
  def resolveTeams(userId: UserId): Result[List[Team]]
}

class UserResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService,
    private val teamService: ITeamService
) extends IUserResolver {
  def me: Task[User] = Auth.currentUser.map(_.toEntity)

  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]] = discordUserService.get(discordId).map(_.map(_.toEntity))
  def resolveTeams(userId: UserId): Result[List[Team]]                   = teamService.getTeamsForUser(userId).mapBoth(_ => UnknownError, _.map(_.toEntity))
}

object UserResolver {
  private type In = IUserService with IDiscordUserService with ITeamService
  private def create(userService: IUserService, discordUserService: IDiscordUserService, teamService: ITeamService) = new UserResolver(discordUserService, userService, teamService)

  val live: ZLayer[In, Throwable, IUserResolver] = ZLayer.fromFunction(create _)

  def apply() = ZIO.service[IUserResolver]
}
