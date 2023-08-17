package venix.hookla.resolvers

import venix.hookla.{Result, Task}
import venix.hookla.entities.{Hook, Team, User}
import venix.hookla.http.Auth
import venix.hookla.services.db.{IHookService, ITeamService, IUserService}
import venix.hookla.services.http.IDiscordUserService
import venix.hookla.types.TeamId
import zio.ZLayer

trait ITeamResolver {
  def getForMe: Task[List[Team]]

  def resolveMembers(id: TeamId): Result[List[User]]
  def resolveHooks(id: TeamId): Result[List[Hook]]
}

class TeamResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService,
    private val teamService: ITeamService,
    private val hookService: IHookService
) extends ITeamResolver {
  def getForMe: Task[List[Team]] = Auth.currentUser.flatMap(user => teamService.getTeamsForUser(user.id).map(_.map(_.toEntity)))

  def resolveMembers(id: TeamId): Result[List[User]] = teamService.getMembers(id).map(_.map { case (user, isAdmin) => user.toEntity(isAdmin) })
  def resolveHooks(id: TeamId): Result[List[Hook]]   = hookService.getByTeam(id).map(_.map(_.toEntity))
}

object TeamResolver {
  private type In = IUserService with IDiscordUserService with ITeamService with IHookService
  private def create(userService: IUserService, discordUserService: IDiscordUserService, teamService: ITeamService, hookService: IHookService) = new TeamResolver(discordUserService, userService, teamService, hookService)

  val live: ZLayer[In, Throwable, ITeamResolver] = ZLayer.fromFunction(create _)
}
