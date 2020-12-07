package venix.hookla.modules

import com.google.inject.AbstractModule
import io.circe.config.parser
import io.circe.generic.auto._
import io.getquill.{CamelCase, PostgresAsyncContext}
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import venix.hookla.HooklaConfig
import venix.hookla.util.play.AkkaGuiceSupport

class MainModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
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