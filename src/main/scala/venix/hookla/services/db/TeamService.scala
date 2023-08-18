package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.RequestError.DatabaseError
import venix.hookla.Result
import venix.hookla.types.{TeamId, UserId}
import zio.ZLayer

trait TeamService extends BaseDBService {
  def getById(id: TeamId): Result[Option[Team]]
  def getTeamsForUser(id: UserId): Result[List[Team]]
  def getMembers(id: TeamId): Result[List[(User, Boolean)]]

  def create(name: String, description: String, userId: UserId): Result[Team]
  def update(id: TeamId, name: Option[String], description: Option[String]): Result[Unit]
  def addMember(teamId: TeamId, userId: UserId, admin: Boolean = false): Result[Unit]
  def updateMember(teamId: TeamId, userId: UserId, admin: Boolean): Result[Unit]
  def removeMember(teamId: TeamId, userId: UserId): Result[Unit]
  def delete(id: TeamId): Result[Unit]
}

private class TeamServiceImpl(private val ctx: ZioJAsyncConnection) extends TeamService {
  import venix.hookla.QuillContext._

  def getById(id: TeamId) =
    run {
      teams.filter(_.id == lift(id))
    }
      .mapBoth(DatabaseError, _.headOption)
      .provide(ZLayer.succeed(ctx))

  def getTeamsForUser(id: UserId) =
    run {
      teamUsers
        .join(teams)
        .on { case (tu, t) => tu.teamId == t.id }
        .filter { case (tu, _) => tu.userId == lift(id) }
    }
      .mapBoth(DatabaseError, _.map(_._2).toList)
      .provide(ZLayer.succeed(ctx))

  def getMembers(id: TeamId) =
    run {
      teamUsers
        .join(users)
        .on { case (tu, u) => tu.userId == u.id }
        .filter { case (tu, _) => tu.teamId == lift(id) }
    }
      .mapBoth(DatabaseError, _.map { case (tu, u) => (u, tu.admin) }.toList)
      .provide(ZLayer.succeed(ctx))

  def create(name: String, description: String, userId: UserId) =
    run {
      teams
        .insert(_.name -> lift(name))
        .returning(x => x)
    }
      .mapError(DatabaseError)
      .tap { t =>
        run {
          teamUsers
            .insert(
              _.teamId -> lift(t.id),
              _.userId -> lift(userId),
              _.admin  -> lift(true)
            )
        }
          .mapError(DatabaseError)
      }
      .provide(ZLayer.succeed(ctx))

  def update(id: TeamId, name: Option[String], description: Option[String]) =
    run {
      teams.dynamic
        .filter(_.id == lift(id))
        .update(setOpt(_.name, name))
    }
      .mapBoth(DatabaseError, _ => ())
      .provide(ZLayer.succeed(ctx))

  def addMember(teamId: TeamId, userId: UserId, admin: Boolean = false) =
    run {
      teamUsers
        .insert(
          _.teamId -> lift(teamId),
          _.userId -> lift(userId),
          _.admin  -> lift(admin)
        )
    }
      .mapBoth(DatabaseError, _ => ())
      .provide(ZLayer.succeed(ctx))

  def updateMember(teamId: TeamId, userId: UserId, admin: Boolean) =
    run {
      teamUsers
        .filter(_.teamId == lift(teamId))
        .filter(_.userId == lift(userId))
        .update(_.admin -> lift(admin))
    }
      .mapBoth(DatabaseError, _ => ())
      .provide(ZLayer.succeed(ctx))

  def removeMember(teamId: TeamId, userId: UserId) =
    run {
      teamUsers
        .filter(_.teamId == lift(teamId))
        .filter(_.userId == lift(userId))
        .delete
    }
      .mapBoth(DatabaseError, _ => ())
      .provide(ZLayer.succeed(ctx))

  def delete(id: TeamId) =
    run {
      teams
        .filter(_.id == lift(id))
        .delete
    }
      .mapBoth(DatabaseError, _ => ())
      .provide(ZLayer.succeed(ctx))
}

object TeamService {
  private type In = ZioJAsyncConnection
  private def create(connection: ZioJAsyncConnection) = new TeamServiceImpl(connection)

  val live: ZLayer[In, Throwable, TeamService] = ZLayer.fromFunction(create _)
}
