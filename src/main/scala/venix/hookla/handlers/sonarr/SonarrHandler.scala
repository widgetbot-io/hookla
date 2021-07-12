package venix.hookla.handlers.sonarr

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.{EventData, Provider}
import venix.hookla.types.providers.SonarrPayload

class SonarrHandler(
  discordMessageService: DiscordMessageService
) extends BaseHandler[SonarrPayload] {
  def handle(payload: SonarrPayload, data: EventData): Unit = ???
}

object SonarrHandler {
  val provider = Provider(
    "sonarr",
    "Sonarr",
    "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
    "eventType",
    isBody = true
  )
}