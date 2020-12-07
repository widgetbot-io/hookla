package venix.hookla.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

// Trait that is extended by other providers
trait EventHandlerCommand

object EventHandler {
  def apply(
    gitlab: ActorRef[Gitlab.Event]
  ): Behavior[EventHandlerCommand] =
    Behaviors.setup { context =>
      new EventHandlerBehaviour(
        context,
        gitlab
      )
    }

  class EventHandlerBehaviour(context: ActorContext[EventHandlerCommand], gitlabActor: ActorRef[Gitlab.Event]) extends AbstractBehavior[EventHandlerCommand](context) {
    override def onMessage(msg: EventHandlerCommand): Behavior[EventHandlerCommand] =
      msg match {
        case event: Gitlab.Event =>
          gitlabActor ! event
          this
      }
  }
}