package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.types.{TeamId, UserId}
import zio.{Task, ZLayer}

trait ITeamService extends BaseDBService {
  def getById(id: TeamId): Task[Option[Team]]
  def getTeamsForUser(id: UserId): Task[List[Team]]
  def getMembers(id: TeamId): Task[List[User]]
}

class TeamService(
    private val ctx: ZioJAsyncConnection
) extends ITeamService {
  import venix.hookla.QuillContext._

  def getById(id: TeamId): Task[Option[Team]] =
    run {
      teams.filter(_.id == lift(id))
    }
      .map(_.headOption)
      .provide(ZLayer.succeed(ctx))

  def getTeamsForUser(id: UserId): Task[List[Team]] =
    run {
      teamUsers
        .join(teams)
        .on { case (tu, t) => tu.teamId == t.id }
        .filter { case (tu, _) => tu.userId == lift(id) }
    }
      .map(_.map(_._2).toList)
      .provide(ZLayer.succeed(ctx))

  // TODO; Should probably be Option[List[User]]
  def getMembers(id: TeamId): Task[List[User]] =
    run {
      teamUsers
        .join(users)
        .on { case (tu, u) => tu.userId == u.id }
        .filter { case (tu, _) => tu.teamId == lift(id) }
    }
      .map(_.map(_._2).toList)
      .provide(ZLayer.succeed(ctx))
}

object TeamService {
  private type In = ZioJAsyncConnection

  private def create(connection: ZioJAsyncConnection) = new TeamService(connection)

  val live: ZLayer[In, Throwable, ITeamService] = ZLayer.fromFunction(create _)
}
