package venix.hookla.resolvers

import venix.hookla.RequestError._
import venix.hookla.Result
import venix.hookla.entities.{Hook, User}
import venix.hookla.services.db.{IHookService, ITeamService, IUserService}
import venix.hookla.services.http.IDiscordUserService
import venix.hookla.types.TeamId
import zio.ZLayer

trait ITeamResolver {
  def resolveMembers(id: TeamId): Result[List[User]]
  def resolveHooks(id: TeamId): Result[List[Hook]]
}

class TeamResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService,
    private val teamService: ITeamService,
    private val hookService: IHookService
) extends ITeamResolver {
  def resolveMembers(id: TeamId): Result[List[User]] = teamService.getMembers(id).mapBoth(_ => UnknownError, _.map { case (user, isAdmin) => user.toEntity(isAdmin) })
  def resolveHooks(id: TeamId): Result[List[Hook]]   = hookService.getByTeam(id).mapBoth(_ => UnknownError, _.map(_.toEntity))
}

object TeamResolver {
  private type In = IUserService with IDiscordUserService with ITeamService with IHookService
  private def create(userService: IUserService, discordUserService: IDiscordUserService, teamService: ITeamService, hookService: IHookService) = new TeamResolver(discordUserService, userService, teamService, hookService)

  val live: ZLayer[In, Throwable, ITeamResolver] = ZLayer.fromFunction(create _)
}
