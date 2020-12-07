package venix.hookla.services

import com.google.inject.Inject
import io.getquill.{CamelCase, PostgresAsyncContext}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import venix.hookla.models.User

class UserService @Inject()(
    dbContext: PostgresAsyncContext[CamelCase]
)(
  implicit executionContext: ExecutionContext
) {
  import dbContext._

  private val users = quote(querySchema[User]("users"))

  def getById(id: UUID): Future[Option[User]] =
    dbContext.run(users.filter(_.id == lift(id))).map(_.headOption)

  def getByDiscordId(discordId: String): Future[Option[User]] =
    dbContext.run(users.filter(_.discordId == lift(discordId))).map(_.headOption)
}
