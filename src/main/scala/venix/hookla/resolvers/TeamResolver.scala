package venix.hookla.resolvers

import venix.hookla.RequestError.Forbidden
import venix.hookla.{Result, Task}
import venix.hookla.entities.{Hook, Team, User}
import venix.hookla.http.Auth
import venix.hookla.services.db.{HookService, TeamService, UserService}
import venix.hookla.services.http.DiscordUserService
import venix.hookla.types.{TeamId, UserId}
import zio.{ZIO, ZLayer}

trait ITeamResolver {
  // Queries
  def getForMe: Task[List[Team]]

  // Mutations
  def create(name: String): Task[Team]
  def update(id: TeamId, name: String): Task[Team]
  def delete(id: TeamId): Task[Unit]
  def addMember(id: TeamId, userId: UserId): Task[Unit]
  def updateMember(id: TeamId, userId: UserId, admin: Boolean): Task[Unit]
  def removeMember(id: TeamId, userId: UserId): Task[Unit]

  // Resolvers
  def resolveMembers(id: TeamId): Result[List[User]]
  def resolveHooks(id: TeamId): Result[List[Hook]]
}

class TeamResolver(
                    private val discordUserService: DiscordUserService,
                    private val userService: UserService,
                    private val teamService: TeamService,
                    private val hookService: HookService
) extends ITeamResolver {
  def getForMe: Task[List[Team]] = Auth.currentUser.flatMap(user => teamService.getTeamsForUser(user.id).map(_.map(_.toEntity)))

  def create(name: String): Task[Team] = Auth.currentUser.flatMap(user => teamService.create(name, "No description specified.", user.id).map(_.toEntity))

  def update(id: TeamId, name: String): Task[Team] = Auth.currentUser.flatMap { user =>
    teamService.getById(id).flatMap {
      case Some(team) =>
        teamService.getMembers(team.id).flatMap { members =>
          if (members.exists { case (u, isAdmin) => u.id == user.id && isAdmin }) {
            teamService.update(id, Some(name), None).as(team.toEntity.copy(name = name))
          } else {
            ZIO.fail(Forbidden("You do not have the required permissions to update this team."))
          }
        }
      case _ => ZIO.fail(Forbidden("You do not have the required permissions to update this team."))
    }
  }

  def delete(id: TeamId): Task[Unit] = Auth.currentUser.flatMap { user =>
    teamService.getById(id).flatMap {
      case Some(team) =>
        teamService.getMembers(team.id).flatMap { members =>
          if (members.exists { case (u, isAdmin) => u.id == user.id && isAdmin }) {
            teamService.delete(id)
          } else {
            ZIO.fail(Forbidden("You do not have the required permissions to delete this team."))
          }
        }
      case _ => ZIO.fail(Forbidden("You do not have the required permissions to delete this team."))
    }
  }

  def addMember(id: TeamId, userId: UserId): Task[Unit] = Auth.currentUser.flatMap { user =>
    teamService.getById(id).flatMap {
      case Some(team) =>
        teamService.getMembers(team.id).flatMap { members =>
          if (members.exists { case (u, isAdmin) => u.id == user.id && isAdmin }) {
            userService.getById(userId).flatMap {
              case Some(user) => teamService.addMember(team.id, user.id)
              case _          => ZIO.fail(Forbidden("The user you are trying to add does not exist."))
            }
          } else {
            ZIO.fail(Forbidden("You do not have the required permissions to add a user to this team."))
          }
        }
      case _ => ZIO.fail(Forbidden("You do not have the required permissions to add a user to this team."))
    }
  }

  def updateMember(id: TeamId, userId: UserId, admin: Boolean): Task[Unit] = Auth.currentUser.flatMap { user =>
    teamService.getById(id).flatMap {
      case Some(team) =>
        teamService.getMembers(team.id).flatMap { members =>
          if (members.exists { case (u, isAdmin) => u.id == user.id && isAdmin }) {
            userService.getById(userId).flatMap {
              case Some(user) => teamService.updateMember(team.id, user.id, admin)
              case _          => ZIO.fail(Forbidden("The user you are trying to update does not exist."))
            }
          } else {
            ZIO.fail(Forbidden("You do not have the required permissions to update a user in this team."))
          }
        }
      case _ => ZIO.fail(Forbidden("You do not have the required permissions to update a user in this team."))
    }
  }

  def removeMember(id: TeamId, userId: UserId): Task[Unit] = Auth.currentUser.flatMap { user =>
    teamService.getById(id).flatMap {
      case Some(team) =>
        teamService.getMembers(team.id).flatMap { members =>
          if (members.exists { case (u, isAdmin) => u.id == user.id && isAdmin }) {
            userService.getById(userId).flatMap {
              case Some(user) => teamService.removeMember(team.id, user.id)
              case _          => ZIO.fail(Forbidden("The user you are trying to remove does not exist."))
            }
          } else {
            ZIO.fail(Forbidden("You do not have the required permissions to remove a user from this team."))
          }
        }
      case _ => ZIO.fail(Forbidden("You do not have the required permissions to remove a user from this team."))
    }
  }

  def resolveMembers(id: TeamId): Result[List[User]] = teamService.getMembers(id).map(_.map { case (user, isAdmin) => user.toEntity(isAdmin) })
  def resolveHooks(id: TeamId): Result[List[Hook]]   = hookService.getByTeam(id).map(_.map(_.toEntity))
}

object TeamResolver {
  private type In = UserService with DiscordUserService with TeamService with HookService
  private def create(userService: UserService, discordUserService: DiscordUserService, teamService: TeamService, hookService: HookService) = new TeamResolver(discordUserService, userService, teamService, hookService)

  val live: ZLayer[In, Throwable, ITeamResolver] = ZLayer.fromFunction(create _)
}
