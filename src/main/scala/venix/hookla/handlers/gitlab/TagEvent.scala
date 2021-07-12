package venix.hookla.handlers.gitlab

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.HandlerData
import venix.hookla.types.providers.GitlabTagPushPayload
import venix.hookla.util.Colours

class TagEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabTagPushPayload] {
  def handleEvent(payload: GitlabTagPushPayload, data: HandlerData) = {
    val refName = getBranchFromRef(payload.ref)

    if (payload.before == "0000000000000000000000000000000000000000") { // Created
      discordMessageService.sendMessageToDiscord(
        data.hook,
        OutgoingEmbed(
          description = Some(s"Tag created: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.CREATED),
          footer = Some(
            OutgoingEmbedFooter(
              s"${payload.project.path_with_namespace}:$refName",
              Some(GitlabHandler.provider.logo)
            )
          )
        )
      )
    } else if (payload.after == "0000000000000000000000000000000000000000") { // Deleted
      discordMessageService.sendMessageToDiscord(
        data.hook,
        OutgoingEmbed(
          description = Some(s"Tag deleted: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.DELETED),
          footer = Some(
            OutgoingEmbedFooter(
              s"${payload.project.path_with_namespace}:$refName",
              Some(GitlabHandler.provider.logo)
            )
          )
        )
      )
    }
  }
}
