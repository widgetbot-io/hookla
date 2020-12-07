package venix.hookla.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Gitlab {
  sealed trait Event extends EventHandlerCommand

  val provider = Provider(
    "gitlab",
    "Gitlab",
    "https://i.viction.dev/assets/images/avi.png",
    eventHeader = Some("X-Gitlab-Event")
  )

  final case class PushEvent(eee: String) extends Event
}

object GitlabEventHandler {
  def apply(discord: ActorRef[Discord.Command]): Behavior[Gitlab.Event] =
    Behaviors.setup(ctx => new GitlabEventHandlerBehaviour(ctx, discord))

  class GitlabEventHandlerBehaviour(context: ActorContext[Gitlab.Event], discord: ActorRef[Discord.Command]) extends AbstractBehavior[Gitlab.Event](context) {
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