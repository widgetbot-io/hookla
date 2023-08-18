package venix.hookla.services.db

import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.RequestError.DatabaseError
import venix.hookla.Result
import venix.hookla.types.UserId
import zio.ZLayer

trait IUserService extends BaseDBService {
  def getById(id: UserId): Result[Option[User]]
}

class UserService(
    private val ctx: ZioJAsyncConnection
) extends IUserService {
  import venix.hookla.QuillContext._

  def getById(id: UserId) =
    run {
      users.filter(_.id == lift(id))
    }
      .mapBoth(DatabaseError, _.headOption)
      .provide(ZLayer.succeed(ctx))
}

object UserService {
  private type In = ZioJAsyncConnection

  private def create(connection: ZioJAsyncConnection) = new UserService(connection)

  val live: ZLayer[In, Throwable, IUserService] = ZLayer.fromFunction(create _)
}
