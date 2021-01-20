package venix.hookla.actors

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import java.time.OffsetDateTime
import venix.hookla.actors.Discord.SendEmbedToDiscord
import venix.hookla.actors.Github.provider
import venix.hookla.models.{DiscordWebhook, EmbedOptions}
import venix.hookla.types.{GitlabCommit, GitlabIssuePayload, GitlabJobPayload, GitlabNotePayload, GitlabPushPayload, GitlabTagPushPayload, Provider}
import venix.hookla.util.Colours

object Gitlab {
  sealed trait Event extends EventHandlerCommand

  val provider = Provider(
    "gitlab",
    "Gitlab",
    "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/GitLab_Logo.svg/1108px-GitLab_Logo.svg.png",
    eventHeader = Some("X-Gitlab-Event")
  )

  final case class NoteEvent(payload: GitlabNotePayload, discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions])   extends Event
  final case class PushEvent(payload: GitlabPushPayload, discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions])   extends Event
  final case class TagEvent(payload: GitlabTagPushPayload, discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions]) extends Event
  final case class IssueEvent(payload: GitlabIssuePayload, discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions]) extends Event
  final case class JobEvent(payload: GitlabJobPayload, discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions])     extends Event
}

object GitlabEventHandler {
  def apply(discord: ActorRef[Discord.Command]): Behavior[Gitlab.Event] =
    Behaviors.setup(ctx => new GitlabEventHandlerBehaviour(ctx, discord))

  class GitlabEventHandlerBehaviour(context: ActorContext[Gitlab.Event], discord: ActorRef[Discord.Command]) extends AbstractBehavior[Gitlab.Event](context) with EventHandlerUtils {
    import Gitlab._

    override def onMessage(e: Event): Behavior[Event] =
      e match {
        case NoteEvent(payload, discordWebhook, embedOptions) =>
          var title = "Unknown"
          var url = payload.project.web_url

          payload.object_attributes.noteable_type match { // If it matches, we can .get
            case "Commit" =>
              title = s"Commit (${payload.commit.get.id.substring(7)})"
            case "MergeRequest" =>
              title = s"Merge Request #${payload.merge_request.get.iid}"
              url = payload.object_attributes.url
            case "Issue" => ???
            case "Snippet" => ???
          }
          discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
            title = Some(title),
            description = Some(payload.object_attributes.note),
            author = Some(OutgoingEmbedAuthor(payload.user.name, None, Some(payload.user.avatar_url))),
            url = Some(url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.NOTE),
            footer = Some(OutgoingEmbedFooter(payload.project.path_with_namespace, Some(Gitlab.provider.logo)))
          ))
          this

        case PushEvent(payload, discordWebhook, embedOptions) =>
          val branchName = getBranchFromRef(payload.ref)
          if (isPrivateBranch(branchName)) return this
          val groupedCommits = payload.commits.groupBy(_.author.email).toList.map(_._2)

          handleBranches(payload, discordWebhook)

          groupedCommits.length match {
            case 1 =>
              val description =
                groupedCommits.head
                  .map(c => formatCommit(c.message, groupedCommits.head.length, embedOptions))
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
                    d.map(c => formatCommit(c.message, d.length, embedOptions)).mkString("\n"),
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
        case TagEvent(payload, discordWebhook, embedOptions) =>
          handleTags(payload, discordWebhook)

          this
        case IssueEvent(_, _, _) =>
          this

        case JobEvent(payload, discordWebhook, embedOptions) =>
          println(payload)

          payload.build_status match {
            case "failed" =>
              if (payload.build_allow_failure) return this

              discord ! SendEmbedToDiscord(discordWebhook, makeJobEmbed(
                payload = payload,
                colour = Colours.FAILED,
                description = "The job has failed."
              ))

            case "canceled" =>
              discord ! SendEmbedToDiscord(discordWebhook, makeJobEmbed(
                payload = payload,
                colour = Colours.CANCELED,
                description = "The job has has been canceled."
              ))

            case "running" =>
              if (payload.build_name.startsWith("deploy-")) {
                val environment = payload.build_name.substring(7)

                if (environment.nonEmpty) {
                  val embed = if (payload.tag) {
                    makeJobEmbed(
                      payload = payload,
                      colour = Colours.RUNNING,
                      description = s"Version ${payload.ref} is deploying to ${environment}..."
                    )
                  } else {
                    makeJobEmbed(
                      payload = payload,
                      colour = Colours.CANCELED,
                      description = s"Deploying latest commit to ${environment}..."
                    )
                  }

                  discord ! SendEmbedToDiscord(discordWebhook, embed)
                }
              }

            case "success" =>
              if (payload.build_name.startsWith("deploy-")) {
                val environment = payload.build_name.substring(7)

                if (environment.nonEmpty) {
                  val embed = if (payload.tag) {
                    makeJobEmbed(
                      payload = payload,
                      colour = Colours.RUNNING,
                      description = s"Version ${payload.ref} has been deployed to ${environment}."
                    )
                  } else {
                    makeJobEmbed(
                      payload = payload,
                      colour = Colours.CANCELED,
                      description = s"Deployed latest commit to ${environment}."
                    )
                  }

                  discord ! SendEmbedToDiscord(discordWebhook, embed)
                }
              }
          }

          this
      }

    def makeJobEmbed(payload: GitlabJobPayload, colour: Int, description: String) = OutgoingEmbed(
      author = Some(OutgoingEmbedAuthor(payload.user.name, None, Some(payload.user.avatar_url))),
      url = Some(s"${payload.repository.homepage}/-/jobs/${payload.build_id}"),
      timestamp = Some(OffsetDateTime.now()),
      footer = Some(OutgoingEmbedFooter(s"${payload.repository.homepage.split("/").drop(3).mkString("/")}:${payload.ref}", Some(Gitlab.provider.logo))),
      color = Some(colour),
      description = Some(description)
    )


    def handleBranches(payload: GitlabPushPayload, discordWebhook: DiscordWebhook) = {
      val refName = getBranchFromRef(payload.ref)

      if (payload.before == "0000000000000000000000000000000000000000") { // Created
        discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
          description = Some(s"Branch created: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.CREATED),
          footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$refName", Some(Gitlab.provider.logo)))
        ))
      } else if (payload.after == "0000000000000000000000000000000000000000") {// Deleted
        discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
          description = Some(s"Branch deleted: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.DELETED),
          footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$refName", Some(Gitlab.provider.logo)))
        ))
      }
    }

    def handleTags(payload: GitlabTagPushPayload, discordWebhook: DiscordWebhook) = {
      val refName = getBranchFromRef(payload.ref)

      if (payload.before == "0000000000000000000000000000000000000000") { // Created
        discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
          description = Some(s"Tag created: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.CREATED),
          footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$refName", Some(Gitlab.provider.logo)))
        ))
      } else if (payload.after == "0000000000000000000000000000000000000000") {// Deleted
        discord ! SendEmbedToDiscord(discordWebhook, OutgoingEmbed(
          description = Some(s"Tag deleted: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.DELETED),
          footer = Some(OutgoingEmbedFooter(s"${payload.project.path_with_namespace}:$refName", Some(Gitlab.provider.logo)))
        ))
      }
    }
  }
}