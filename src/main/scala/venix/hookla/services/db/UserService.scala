package venix.hookla.services.db

import io.getquill.EntityQuery
import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.models.User
import zio.{Task, ZLayer}

import java.util.UUID

class UserService(
    private val ctx: ZioJAsyncConnection
) extends BaseDBService {
  import venix.hookla.QuillContext._

  def getById(id: UUID): Task[Option[User]] =
    run {
      users.filter(_.id == lift(id))
    }
      .map(_.headOption)
      .provide(ZLayer.succeed(ctx))
}

object UserService {
  private type In = ZioJAsyncConnection

  private def create(connection: ZioJAsyncConnection) = new UserService(connection)

  val live: ZLayer[In, Throwable, UserService] = ZLayer.fromFunction(create _)
}
