package venix.hookla.actors

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

// Trait that is extended by other providers
trait EventHandlerCommand

object EventHandler {
  def apply(
    gitlab: ActorRef[Gitlab.Event],
    github: ActorRef[Github.Event]
  ): Behavior[EventHandlerCommand] =
    Behaviors.setup { context =>
      new EventHandlerBehaviour(
        context,
        gitlab,
        github
      )
    }

  class EventHandlerBehaviour(context: ActorContext[EventHandlerCommand], gitlabActor: ActorRef[Gitlab.Event], githubActor: ActorRef[Github.Event]) extends AbstractBehavior[EventHandlerCommand](context) {
    override def onMessage(msg: EventHandlerCommand): Behavior[EventHandlerCommand] =
      msg match {
        case event: Gitlab.Event =>
          gitlabActor ! event
          this
        case event: Github.Event =>
          githubActor ! event
          this
      }
  }
}