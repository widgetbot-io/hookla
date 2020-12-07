package venix.hookla.modules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import venix.hookla.actors.DiscordMessageSender
import akka.actor.typed.{ActorSystem => TypedActorSystem}
import venix.hookla.util.play.AkkaGuiceSupport

class AkkaModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind[TypedActorSystem[NotUsed]].toInstance(TypedActorSystem(Behaviors.ignore, "hookla"))
    bind[ActorSystem].toInstance(ActorSystem("hookla"))
    bindTypedActor(DiscordMessageSender(), "discord-message-sender")
  }
}
