package venix.hookla.handlers.radarr

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{GitlabPayload, RadarrPayload}

class RadarrHandler(
  discordMessageService: DiscordMessageService
) extends BaseHandler[RadarrPayload] {
  def handle(payload: RadarrPayload, data: EventData): Unit = ???
}
