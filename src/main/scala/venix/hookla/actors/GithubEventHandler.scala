package venix.hookla.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Github {
  sealed trait Event extends EventHandlerCommand

  final case class PushEvent(eee: String) extends Event
}

object GithubEventHandler {
  def apply(): Behavior[Github.Event] =
    Behaviors.setup(ctx => new GithubEventHandlerBehaviour(ctx))

  class GithubEventHandlerBehaviour(context: ActorContext[Github.Event]) extends AbstractBehavior[Github.Event](context) {
    import Github._

    override def onMessage(e: Event): Behavior[Event] =
      e match {
        case PushEvent(eee) =>
          println(s"aaaaa: ${eee}")
          // make the embed
          // send to discord
          this
      }
  }
}