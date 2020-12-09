package venix.hookla.actors

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import java.time.OffsetDateTime
import venix.hookla.actors.Discord.SendEmbedToDiscord
import venix.hookla.actors.Github.provider
import venix.hookla.models.DiscordWebhook
import venix.hookla.types.{GitlabNotePayload, GitlabCommit, GitlabIssuePayload, GitlabPushPayload, Provider}
import venix.hookla.util.Colours

object Gitlab {
  sealed trait Event extends EventHandlerCommand

  val provider = Provider(
    "gitlab",
    "Gitlab",
    "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/GitLab_Logo.svg/1108px-GitLab_Logo.svg.png",
    eventHeader = Some("X-Gitlab-Event")
  )

  final case class NoteEvent(payload: GitlabNotePayload, discordWebhook: DiscordWebhook)  extends Event
  final case class PushEvent(payload: GitlabPushPayload, discordWebhook: DiscordWebhook)   extends Event
  final case class IssueEvent(payload: GitlabIssuePayload, discordWebhook: DiscordWebhook) extends Event
}

object GitlabEventHandler {
  def apply(discord: ActorRef[Discord.Command]): Behavior[Gitlab.Event] =
    Behaviors.setup(ctx => new GitlabEventHandlerBehaviour(ctx, discord))

  class GitlabEventHandlerBehaviour(context: ActorContext[Gitlab.Event], discord: ActorRef[Discord.Command]) extends AbstractBehavior[Gitlab.Event](context) with EventHandlerUtils {
    import Gitlab._

    override def onMessage(e: Event): Behavior[Event] =
      e match {
        case NoteEvent(payload, discordWebhook) =>
          var title = "Test"
          val description = payload.object_attributes.note

          payload.object_attributes.noteable_type match { // If it matches, we can .get
            case "Commit" =>
              title = s"Commit (${payload.commit.get.id.substring(7)})"
            case "MergeRequest" =>
              title = s"Merge Request #${payload.merge_request.get.iid}"
            case "Issue" => ???
            case "Snippet" => ???
          }
          discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
            title = Some(title),
            description = Some(description),
            author = Some(OutgoingEmbedAuthor(payload.user.name, None, Some(payload.user.avatar_url))),
            url = Some(payload.project.web_url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.NOTE),
            footer = Some(OutgoingEmbedFooter(payload.project.path_with_namespace, Some(Gitlab.provider.logo)))
          ))
          this

        case PushEvent(payload, discordWebhook) =>
          val branchName = payload.ref.split('/').drop(2).mkString("/")
          val groupedCommits: Seq[Seq[GitlabCommit]] = payload.commits.groupBy(_.author.email).toSeq.map(_._2)

          groupedCommits.length match {
            case 1 =>
              val description =
                groupedCommits.head
                  .map(c => formatCommit(c.message, groupedCommits.head.length))
                  .mkString("\n")

              discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
                description = Some(description),
                author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
                url = Some(payload.project.web_url),
                timestamp = Some(OffsetDateTime.now()),
                color = Some(Colours.PUSH),
                footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$branchName", Some(Gitlab.provider.logo)))
              ))
              this
            case x if(x > 1) =>
              val fields =
                groupedCommits.map { d =>
                  EmbedField(
                    s"Commits from ${d.head.author.name}",
                    d.map(c => formatCommit(c.message, d.length)).mkString("\n"),
                    Some(false)
                  )
                }

              discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
                author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
                url = Some(payload.project.web_url),
                timestamp = Some(OffsetDateTime.now()),
                fields = fields,
                color = Some(Colours.PUSH),
                footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$branchName", Some(Gitlab.provider.logo)))
              ))
              this
            case _ =>
              println("_")
              this
          }
        case IssueEvent(_, _) =>
          this
      }
  }
}