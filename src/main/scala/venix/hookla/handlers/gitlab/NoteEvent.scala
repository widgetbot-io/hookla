package venix.hookla.handlers.gitlab

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.{GithubIssuePayload, GitlabNotePayload, HandlerData}
import venix.hookla.util.Colours

class NoteEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabNotePayload] {
  def handleEvent(payload: GitlabNotePayload, data: HandlerData) = {
    var title = "Unknown"
    var url = payload.project.web_url

    payload.object_attributes.noteable_type match { // If it matches, we can .get
      case "Commit" =>
        title = s"Commit (${payload.commit.get.id.substring(7)})"
      case "MergeRequest" =>
        title = s"Merge Request #${payload.merge_request.get.iid}"
        url = payload.object_attributes.url
      case "Issue" => ??? // TODO: Finish implementing this.
      case "Snippet" => ???
    }
    discordMessageService.sendMessageToDiscord(data.hook, OutgoingEmbed(
      title = Some(title),
      description = Some(payload.object_attributes.note),
      author = Some(OutgoingEmbedAuthor(payload.user.name, None, Some(payload.user.avatar_url))),
      url = Some(url),
      timestamp = Some(OffsetDateTime.now()),
      color = Some(Colours.NOTE),
      footer = Some(OutgoingEmbedFooter(payload.project.path_with_namespace, Some(GitlabHandler.provider.logo)))
    ))
  }
}