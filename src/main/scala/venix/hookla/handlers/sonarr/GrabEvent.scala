package venix.hookla.handlers.sonarr

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{SonarrGrabEvent, SonarrTestEvent}
import venix.hookla.util.Colours

class GrabEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[SonarrGrabEvent] {
  def handleEvent(payload: SonarrGrabEvent, data: EventData): Unit = ???
//    discordMessageService.sendMessageToDiscord(
//      data.hook,
//      OutgoingEmbed(
//        author = Some(OutgoingEmbedAuthor("Episode Downloading...", None, Some(SonarrHandler.provider.logo))),
//        fields = Seq(EmbedField("Aired", payload.episodes.head.airDateUtc)),
//        footer = Some(
//          OutgoingEmbedFooter(
//            "Sonarr",
//            Some(SonarrHandler.provider.logo)
//          )
//        )
//      )
//    )
}
