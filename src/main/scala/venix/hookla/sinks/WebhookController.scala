package venix.hookla.sinks

import venix.hookla.Result
import zio.ZLayer
import zio.http.Request

trait IWebhookController {
  def handleWebhook(request: Request): Result[Unit]
}

/**
  * This class contains the handling of the webhooks that are INCOMING from sources
  * such as GitHub, GitLab, BitBucket, Sonarr, Radarr, etc..
  */
class WebhookController extends IWebhookController {
  def handleWebhook(request: Request): Result[Unit] = ???
}

object WebhookController {
  private type In = Any
  private def create() = new WebhookController()

  val live: zio.ZLayer[In, Throwable, IWebhookController] = ZLayer.fromFunction(create _)
}
