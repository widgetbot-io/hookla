package venix.hookla.handlers.ombi

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{GitlabPayload, OmbiPayload}

class OmbiHandler(
  discordMessageService: DiscordMessageService
) extends BaseHandler[OmbiPayload] {
  def handle(payload: OmbiPayload, data: EventData): Unit = ???
}
