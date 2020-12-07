package venix.hookla.modules

import akka.actor.typed.ActorRef
import com.google.inject.{AbstractModule, Injector}
import net.codingwell.scalaguice.ScalaModule
import venix.hookla.actors._
import venix.hookla.util.play.AkkaGuiceSupport

class ActorModule(injector: Injector) extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  import net.codingwell.scalaguice.InjectorExtensions._

  override def configure(): Unit = {
    val eventHandler = EventHandler(injector.instance[ActorRef[Gitlab.Event]], injector.instance[ActorRef[Github.Event]])

    bindTypedActor(eventHandler, "event-handler")
    bindTypedActor(DiscordMessageSender(), "discord-message-sender")
  }
}
