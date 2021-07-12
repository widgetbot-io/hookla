package venix.hookla.handlers.sonarr

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.SonarrPayload

class SonarrHandler(
  discordMessageService: DiscordMessageService
) extends BaseHandler[SonarrPayload] {
  def handle(payload: SonarrPayload, data: EventData): Unit = ???
}
