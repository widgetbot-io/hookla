package venix.hookla

import akka.NotUsed
import akka.actor.typed.{ActorSystem => TypedActorSystem}
import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import com.twitter.finagle.http
import io.circe.config.parser
import io.getquill.{CamelCase, PostgresAsyncContext, SnakeCase}
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import io.circe.generic.auto._
import venix.hookla.actors.{EventHandler, GitlabEventHandler}
import venix.hookla.util.play.AkkaGuiceSupport
import akka.actor.typed.scaladsl.Behaviors

class MainModule extends AbstractModule with AkkaGuiceSupport with ScalaModule {
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
    bind[TypedActorSystem[NotUsed]].toInstance(TypedActorSystem(Behaviors.ignore, "hookla"))
    bind[ActorSystem].toInstance(ActorSystem("hookla"))

//    val gitlabEventHandler = GitlabEventHandler()
//
//    bindTypedActor(gitlabEventHandler, "gitlab-event-handler")
//
//    val eventHandler = EventHandler(gitlabEventHandler)
  }
}
