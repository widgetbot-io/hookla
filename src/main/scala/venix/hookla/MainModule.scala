package venix.hookla

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem => TypedActorSystem}
import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Injector}
import io.circe.config.parser
import io.getquill.{CamelCase, PostgresAsyncContext}
import net.codingwell.scalaguice.ScalaModule
import scala.concurrent.ExecutionContext
import io.circe.generic.auto._
import venix.hookla.actors.{EventHandler, Github, GithubEventHandler, Gitlab, GitlabEventHandler}
import venix.hookla.util.play.AkkaGuiceSupport
import akka.actor.typed.scaladsl.Behaviors

class AkkaModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind[TypedActorSystem[NotUsed]].toInstance(TypedActorSystem(Behaviors.ignore, "hookla"))
    bind[ActorSystem].toInstance(ActorSystem("hookla"))

    bindTypedActor(GitlabEventHandler(), "gitlab-event-handler")
    bindTypedActor(GithubEventHandler(), "github-event-handler")
  }
}

class BaseModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
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

class ActorModule(injector: Injector) extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  import net.codingwell.scalaguice.InjectorExtensions._

  override def configure(): Unit = {
    val eventHandler = EventHandler(injector.instance[ActorRef[Gitlab.Event]], injector.instance[ActorRef[Github.Event]])

    bindTypedActor(eventHandler, "event-handler")
  }
}
