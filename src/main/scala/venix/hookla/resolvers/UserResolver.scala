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
  def resolveTeams(userId: UserId): Task[List[Team]]
}

class UserResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService,
    private val teamService: ITeamService
) extends IUserResolver {
  def me: Task[User] = Auth.currentUser.map(_.toEntity())

  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]] = discordUserService.get(discordId).map(_.map(_.toEntity))
  def resolveTeams(userId: UserId): Task[List[Team]] = for {
    // TODO: See if you can inject Auth in the constructor rather than changing this to be a Task
    me <- Auth.currentUser
    _  <- ZIO.fail(Forbidden("You can't access this users teams.")) unless (me.id == userId)

    teams <- teamService.getTeamsForUser(userId).map(_.map(_.toEntity))
  } yield teams
}

object UserResolver {
  private type In = IUserService with IDiscordUserService with ITeamService
  private def create(userService: IUserService, discordUserService: IDiscordUserService, teamService: ITeamService) = new UserResolver(discordUserService, userService, teamService)

  val live: ZLayer[In, Throwable, IUserResolver] = ZLayer.fromFunction(create _)

  def apply() = ZIO.service[IUserResolver]
}
