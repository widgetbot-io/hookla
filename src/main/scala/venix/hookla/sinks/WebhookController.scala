package venix.hookla.sinks

import venix.hookla.Result
import zio.ZLayer
import zio.http.Request

trait WebhookController {
  def handleWebhook(request: Request): Result[Unit]
}

/**
  * This class contains the handling of the webhooks that are INCOMING from sources
  * such as GitHub, GitLab, BitBucket, Sonarr, Radarr, etc..
  */
private class WebhookControllerImpl extends WebhookController {
  def handleWebhook(request: Request): Result[Unit] = ???
}

object WebhookController {
  private type In = Any
  private def create() = new WebhookControllerImpl()

  val live: zio.ZLayer[In, Throwable, WebhookController] = ZLayer.fromFunction(create _)
}
