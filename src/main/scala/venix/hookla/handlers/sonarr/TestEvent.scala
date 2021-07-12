package venix.hookla.handlers.sonarr

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.SonarrTestEvent
import venix.hookla.util.Colours

class TestEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[SonarrTestEvent] {
  def handleEvent(payload: SonarrTestEvent, data: EventData): Unit =
    discordMessageService.sendMessageToDiscord(
      data.hook,
      OutgoingEmbed(
        description = Some("Sonarr Test Hook!"),
        author = Some(OutgoingEmbedAuthor("Sonarr", None, Some(SonarrHandler.provider.logo))),
        timestamp = Some(OffsetDateTime.now()),
        color = Some(Colours.CREATED),
        footer = Some(
          OutgoingEmbedFooter(
            "Sonarr",
            Some(SonarrHandler.provider.logo)
          )
        )
      )
    )
}
