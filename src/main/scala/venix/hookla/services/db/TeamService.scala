package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.RequestError.DatabaseError
import venix.hookla.Result
import venix.hookla.types.{TeamId, UserId}
import zio.ZLayer

trait ITeamService extends BaseDBService {
  def getById(id: TeamId): Result[Option[Team]]
  def getTeamsForUser(id: UserId): Result[List[Team]]
  def getMembers(id: TeamId): Result[List[(User, Boolean)]]
}

class TeamService(
    private val ctx: ZioJAsyncConnection
) extends ITeamService {
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
}

object TeamService {
  private type In = ZioJAsyncConnection

  private def create(connection: ZioJAsyncConnection) = new TeamService(connection)

  val live: ZLayer[In, Throwable, ITeamService] = ZLayer.fromFunction(create _)
}
