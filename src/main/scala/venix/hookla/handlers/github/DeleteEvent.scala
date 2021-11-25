package venix.hookla.handlers.github

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{GithubCreatePayload, GithubDeletePayload, GithubRefType}
import venix.hookla.util.Colours

class DeleteEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GithubDeletePayload] {
  def handleEvent(payload: GithubDeletePayload, data: EventData) = {
    payload.ref_type match {
      case GithubRefType.Branch =>
        val ref = payload.ref

        discordMessageService.sendMessageToDiscord(
          data.hook,
          OutgoingEmbed(
            description = Some(s"Branch deleted: $ref"),
            author = Some(OutgoingEmbedAuthor(payload.sender.login, None, Some(payload.sender.avatar_url))),
            url = Some(payload.repository.html_url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.CREATED),
            footer = Some(
              OutgoingEmbedFooter(
                s"${payload.repository.full_name}:$ref",
                Some(GithubHandler.provider.logo)
              )
            )
          )
        )
      case GithubRefType.Tag =>
        discordMessageService.sendMessageToDiscord(
          data.hook,
          OutgoingEmbed(
            description = Some(s"Tag deleted: ${payload.ref}"),
            author = Some(OutgoingEmbedAuthor(payload.sender.login, None, Some(payload.sender.avatar_url))),
            url = Some(payload.repository.html_url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.CREATED),
            footer = Some(
              OutgoingEmbedFooter(
                s"${payload.repository.full_name}:${payload.ref}",
                Some(GithubHandler.provider.logo)
              )
            )
          )
        )
      case _ =>
    }
  }
}
