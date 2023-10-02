package venix.hookla.sources

import io.circe.Json
import io.circe.syntax._
import sttp.tapir.{Endpoint, PublicEndpoint}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import venix.hookla.Env
import venix.hookla.RequestError.BadRequest
import venix.hookla.services.db.HookService
import venix.hookla.types.HookId
import zio.http.{Body, HttpApp, Request, Response, Status}
import zio.{&, RIO, ZIO, ZLayer}
import sttp.tapir.ztapir._

import java.util.UUID

trait WebhookController {
  def makeHttpService[R](implicit serverOptions: ZioHttpServerOptions[R] = ZioHttpServerOptions.default[R]): HttpApp[R & Env, Throwable]
}

/**
  * This class contains the handling of the webhooks that are INCOMING from sources
  * such as GitHub, GitLab, BitBucket, Sonarr, Radarr, etc..
  */
private class WebhookControllerImpl(
    private val hookService: HookService
) extends WebhookController {
  // URI: /api/v1/handle/:hookId
  // Method: POST
  private def handleWebhook(request: Request, body: String): ZIO[Env, String, String] = (for {
    _ <- ZIO.unit // just to start the for comprehension

    maybeHookId = request.url.path.dropTrailingSlash.last
    _ <- ZIO.fail(BadRequest("You need to pass a webhook ID.")) when maybeHookId.isEmpty

    // TODO: This is a bit ugly, but it works for now.
    hookId <- ZIO.attempt(UUID.fromString(maybeHookId.get)).map(HookId(_))

    hook <- hookService.get(hookId)
    _    <- ZIO.fail(BadRequest("Invalid webhook ID.")) when hook.isEmpty
    // TODO: Update last used timestamp

    handler      <- SourceHandler.getHandlerById(hook.get.sourceId)
    eventHandler <- handler.determineEvent(request)

    // TODO: This needs to be abstracted out to support non-JSON body's like the handler traits have.
    jsonBody <- ZIO.attempt(body.asJson)

    _ <- eventHandler
      .asInstanceOf[GithubSourceEventHandler]
      .handle(jsonBody, request.headers.map(x => x.headerName -> x.renderedValue).toMap, hook.get)
  } yield Json.obj("message" -> Json.fromString("Success!")).spaces2).mapError { e => println(e); "temp" } // TODO: Figure out how to have better errors here.

  private def webhookEndpoint = endpoint
    .in(extractFromRequest[Request](x => x.underlying.asInstanceOf[Request]))
    .in(stringJsonBody)
    .errorOut(stringBody)
    .out(stringJsonBody)

  def makeHttpService[R](implicit serverOptions: ZioHttpServerOptions[R]): HttpApp[R & Env, Throwable] =
    ZioHttpInterpreter(serverOptions)
      .toHttp(webhookEndpoint.zServerLogic(c => handleWebhook(c._1, c._2)))
}

object WebhookController {
  private type In = HookService
  private def create(hookService: HookService) = new WebhookControllerImpl(hookService)

  val live: zio.ZLayer[In, Throwable, WebhookController] = ZLayer.fromFunction(create _)
}
