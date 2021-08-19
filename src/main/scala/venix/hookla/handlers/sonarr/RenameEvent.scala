package venix.hookla.handlers.sonarr

import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{SonarrDownloadEvent, SonarrRenameEvent}

class RenameEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[SonarrRenameEvent] {
  def handleEvent(payload: SonarrRenameEvent, data: EventData): Unit = ???
}
