package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.RequestError.DatabaseError
import venix.hookla.Result
import venix.hookla.types.{HookId, TeamId}
import zio.ZLayer

trait IHookService extends BaseDBService {
  def get(team: TeamId, hook: HookId): Result[Option[Hook]]
  def getUnsafe(hook: HookId): Result[Hook]

  def getByTeam(team: TeamId): Result[List[Hook]]
}

class HookService(
    private val ctx: ZioJAsyncConnection
) extends IHookService {
  import venix.hookla.QuillContext._

  def get(team: TeamId, hook: HookId) =
    run {
      hooks
        .filter(_.id == lift(hook))
        .filter(_.teamId == lift(team))
    }
      .mapBoth(DatabaseError, _.headOption)
      .provide(ZLayer.succeed(ctx))

  def getUnsafe(hook: HookId) =
    run {
      hooks
        .filter(_.id == lift(hook))
    }
      .mapBoth(DatabaseError, _.head)
      .provide(ZLayer.succeed(ctx))

  def getByTeam(team: TeamId) =
    run {
      hooks
        .filter(_.teamId == lift(team))
    }
      .mapBoth(DatabaseError, _.toList)
      .provide(ZLayer.succeed(ctx))
}

object HookService {
  private type In = ZioJAsyncConnection

  private def create(connection: ZioJAsyncConnection) = new HookService(connection)

  val live: ZLayer[In, Throwable, IHookService] = ZLayer.fromFunction(create _)
}
