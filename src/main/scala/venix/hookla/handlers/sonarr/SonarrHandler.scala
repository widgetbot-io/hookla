package venix.hookla.handlers.sonarr

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.{EventData, Provider}
import venix.hookla.types.providers.{SonarrDownloadEvent, SonarrGrabEvent, SonarrPayload, SonarrRenameEvent, SonarrTestEvent}

class SonarrHandler(
  discordMessageService: DiscordMessageService
) extends BaseHandler[SonarrPayload] {
  import com.softwaremill.macwire._

  def handle(payload: SonarrPayload, data: EventData): Unit = payload match {
    case payload: SonarrGrabEvent => wire[GrabEvent].handleEvent(payload, data)
    case payload: SonarrDownloadEvent => wire[DownloadEvent].handleEvent(payload, data)
    case payload: SonarrRenameEvent => wire[RenameEvent].handleEvent(payload, data)
    case payload: SonarrTestEvent => wire[TestEvent].handleEvent(payload, data)
  }
}

object SonarrHandler {
  val provider = Provider(
    "sonarr",
    "Sonarr",
    "https://forums-sonarr-tv.s3.dualstack.us-east-1.amazonaws.com/original/2X/e/ef4553fe96f04a298ec502279731579698e96a9b.png",
    "eventType",
    isBody = true
  )
}