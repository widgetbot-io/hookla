package venix.hookla.services

import io.getquill.{CamelCase, PostgresAsyncContext}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import venix.hookla.models.EmbedOptions

class EmbedOptionsService @Inject()(
    dbContext: PostgresAsyncContext[CamelCase]
)(
    implicit executionContext: ExecutionContext
) {
  import dbContext._

  private val embedOptions = quote(querySchema[EmbedOptions]("embed_options"))

  def getById(id: UUID): Future[Option[EmbedOptions]] =
    dbContext.run(embedOptions.filter(_.id == lift(id))).map(_.headOption)

  def getEmbedOptionsByUser(userId: UUID): Future[List[EmbedOptions]] =
    dbContext.run(embedOptions.filter(_.userId == lift(userId))).map(_.toList)
}
