package venix.hookla.resolvers

import venix.hookla.RequestError._
import venix.hookla.{Result, Task}
import venix.hookla.entities.{DiscordUser, Team, User}
import venix.hookla.http.Auth
import venix.hookla.services.db.{TeamService, UserService}
import venix.hookla.services.http.DiscordUserService
import venix.hookla.types.UserId
import zio.{ZIO, ZLayer}

trait UserResolver {
  def me: Task[User]

  def resolveDiscordUser(discordId: String): Result[Option[DiscordUser]]
  def resolveTeams(userId: UserId): Task[List[Team]]
}

private class UserResolverImpl(
    private val discordUserService: DiscordUserService,
    private val userService: UserService,
    private val teamService: TeamService
) extends UserResolver {
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
  private type In = UserService with DiscordUserService with TeamService
  private def create(userService: UserService, discordUserService: DiscordUserService, teamService: TeamService) = new UserResolverImpl(discordUserService, userService, teamService)

  val live: ZLayer[In, Throwable, UserResolver] = ZLayer.fromFunction(create _)

  def apply() = ZIO.service[UserResolver]
}
