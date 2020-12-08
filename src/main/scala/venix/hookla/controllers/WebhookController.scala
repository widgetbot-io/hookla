package venix.hookla.controllers

import akka.actor.typed.ActorRef
import cats.effect._
import com.google.inject.Inject
import io.circe.Json
import io.finch._
import io.finch.circe._
import scala.concurrent.ExecutionContext
import venix.hookla.actors._
import venix.hookla.services.{DiscordWebhookService, ProviderSettingsService}
import venix.hookla.types.GithubPayload
import venix.hookla.types.GithubPayloads._

class WebhookController @Inject()(
  actor: ActorRef[EventHandlerCommand],
  providerSettingsService: ProviderSettingsService,
  discordWebhookService: DiscordWebhookService
)(
    implicit executionContext: ExecutionContext
) extends BaseController {
  def endpoints = process

  def process: Endpoint[IO, String] = post("process" :: path[String] :: jsonBody[Json] :: headersAll) { (token: String, body: Json, headers: Map[String, String]) =>
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        logger.debug(s"fetched data for provider ${providerSettings.slug}")

        body.as[GithubPayload] match {
          case Left(error) =>
            logger.error(error.getMessage())
          case Right(value) =>
            discordWebhookService.getById(providerSettings.discordWebhookId) map {
              case Some(_) =>
                actor ! value.toEvent(_)
            }
        }

        Ok("success")
    }
  }
}
