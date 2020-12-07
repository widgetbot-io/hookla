package venix.hookla.actors

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import java.time.OffsetDateTime
import venix.hookla.actors.Discord.SendEmbedToDiscord
import venix.hookla.models.DiscordWebhook
import venix.hookla.types.{GithubIssuePayload, GithubPushPayload}
import venix.hookla.util.Colours

object Github {
  sealed trait Event extends EventHandlerCommand

  val provider = Provider(
    "github",
    "GitHub",
    "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
    eventHeader = Some("X-GitHub-Event"),
  )

  final case class PushEvent(payload: GithubPushPayload, discordWebhook: DiscordWebhook) extends Event
  final case class IssueEvent(payload: GithubIssuePayload, discordWebhook: DiscordWebhook) extends Event
}

object GithubEventHandler {
  def apply(discord: ActorRef[Discord.Command]): Behavior[Github.Event] =
    Behaviors.setup(ctx => new GithubEventHandlerBehaviour(ctx, discord))

  class GithubEventHandlerBehaviour(context: ActorContext[Github.Event], discord: ActorRef[Discord.Command]) extends AbstractBehavior[Github.Event](context) {
    import Github._

    override def onMessage(e: Event): Behavior[Event] =
      e match {
        case PushEvent(payload, discordWebhook) =>
          val branchName = payload.ref.split('/').drop(2).mkString("/")

          discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
            description = Some("Description type beat"),
            author = Some(OutgoingEmbedAuthor(payload.pusher.name, None, Some(payload.sender.avatar_url))),
            url = Some(payload.repository.html_url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.PUSH),
            footer = Some(OutgoingEmbedFooter(s"${payload.repository.full_name}:$branchName", Some(provider.logo)))
          ))

          this
        case IssueEvent(payload, providerSettings) =>
          println(s"issue $payload $providerSettings")
          this
      }
  }
}