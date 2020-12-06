package venix.hookla

import com.google.inject.AbstractModule
import com.twitter.finagle.http
import io.circe.config.parser
import io.getquill.{CamelCase, PostgresAsyncContext, SnakeCase}
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import io.circe.generic.auto._

class MainModule extends AbstractModule with ScalaModule {
  val config: HooklaConfig =
    parser
      .decode[HooklaConfig]()
      .fold(
        errors => throw errors.fillInStackTrace(),
        identity
      )

  private def providePostgres: PostgresAsyncContext[CamelCase] =
    new PostgresAsyncContext(CamelCase, "postgres")

  override def configure(): Unit = {
    bind[HooklaConfig].toInstance(config)
    bind[ExecutionContext].toInstance(scala.concurrent.ExecutionContext.Implicits.global)
    bind[PostgresAsyncContext[CamelCase]].toInstance(providePostgres)
  }
}
