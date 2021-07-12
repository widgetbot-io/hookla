package venix.hookla.handlers.sonarr

import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{SonarrDownloadEvent, SonarrGrabEvent}

class DownloadEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[SonarrDownloadEvent] {
  def handleEvent(payload: SonarrDownloadEvent, data: EventData): Unit = ???
}
