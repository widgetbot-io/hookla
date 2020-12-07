package venix.hookla.modules

import akka.actor.typed.ActorRef
import com.google.inject.{AbstractModule, Injector}
import net.codingwell.scalaguice.ScalaModule
import venix.hookla.actors._
import venix.hookla.util.play.AkkaGuiceSupport

class ChildActorsModule(injector: Injector) extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  import net.codingwell.scalaguice.InjectorExtensions._

  override def configure(): Unit = {
    bindTypedActor(GitlabEventHandler(injector.instance[ActorRef[Discord.Command]]), "gitlab-event-handler")
    bindTypedActor(GithubEventHandler(injector.instance[ActorRef[Discord.Command]]), "github-event-handler")
  }
}
