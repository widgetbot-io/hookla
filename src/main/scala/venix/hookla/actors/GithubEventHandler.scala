package venix.hookla.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import venix.hookla.models.ProviderSettings
import venix.hookla.types.GithubPushPayload

object Github {
  sealed trait Event extends EventHandlerCommand

  val provider = Provider(
    "github",
    "GitHub",
    "https://i.viction.dev/assets/images/avi.png",
    eventHeader = Some("X-GitHub-Event"),
  )

  final case class PushEvent(payload: GithubPushPayload, providerSettings: ProviderSettings) extends Event
}

object GithubEventHandler {
  def apply(): Behavior[Github.Event] =
    Behaviors.setup(ctx => new GithubEventHandlerBehaviour(ctx))

  class GithubEventHandlerBehaviour(context: ActorContext[Github.Event]) extends AbstractBehavior[Github.Event](context) {
    import Github._

    override def onMessage(e: Event): Behavior[Event] =
      e match {
        case PushEvent(payload, providerSettings) =>
          println(s"aaaaa: $payload $providerSettings")
          // make the embed
          // send to discord
          this
      }
  }
}