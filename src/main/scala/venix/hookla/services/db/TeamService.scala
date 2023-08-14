package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.types.TeamId
import zio.{Task, ZLayer}

import java.util.UUID

class TeamService(
    private val ctx: ZioJAsyncConnection
) extends BaseDBService {
  import venix.hookla.QuillContext._

  def getById(id: TeamId): Task[Option[Team]] =
    run {
      teams.filter(_.id == lift(id))
    }
      .map(_.headOption)
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

  val live: ZLayer[In, Throwable, TeamService] = ZLayer.fromFunction(create _)
}
