package venix.hookla.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Gitlab {
  sealed trait Event extends EventHandlerCommand

  final case class PushEvent(eee: String) extends Event
}

object GitlabEventHandler {
  def apply(): Behavior[Gitlab.Event] =
    Behaviors.setup(ctx => new GitlabEventHandlerBehaviour(ctx))

  class GitlabEventHandlerBehaviour(context: ActorContext[Gitlab.Event]) extends AbstractBehavior[Gitlab.Event](context) {
    import Gitlab._

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