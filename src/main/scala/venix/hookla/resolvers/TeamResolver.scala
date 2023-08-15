package venix.hookla.resolvers

import venix.hookla.RequestError._
import venix.hookla.Result
import venix.hookla.entities.User
import venix.hookla.services.db.{ITeamService, IUserService}
import venix.hookla.services.http.IDiscordUserService
import venix.hookla.types.TeamId
import zio.ZLayer

trait ITeamResolver {
  def resolveMembers(id: TeamId): Result[List[User]]
}

class TeamResolver(
    private val discordUserService: IDiscordUserService,
    private val userService: IUserService,
    private val teamService: ITeamService
) extends ITeamResolver {
  override def resolveMembers(id: TeamId): Result[List[User]] = teamService.getMembers(id).mapBoth(_ => UnknownError, _.map(_.toEntity))
}

object TeamResolver {
  private type In = IUserService with IDiscordUserService with ITeamService
  private def create(userService: IUserService, discordUserService: IDiscordUserService, teamService: ITeamService) = new TeamResolver(discordUserService, userService, teamService)

  val live: ZLayer[In, Throwable, ITeamResolver] = ZLayer.fromFunction(create _)
}
