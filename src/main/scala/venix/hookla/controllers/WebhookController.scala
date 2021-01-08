package venix.hookla.controllers

import akka.actor.typed.ActorRef
import cats.effect._
import com.google.inject.Inject
import io.circe.Decoder.Result
import io.circe.{Decoder, Json}
import io.finch._
import io.finch.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext
import venix.hookla.actors._
import venix.hookla.services.{DiscordWebhookService, ProviderSettingsService}
import venix.hookla.types.{BasePayload, GithubPayload, GithubPayloads, GitlabPayload}
import venix.hookla.types.GithubPayloads._
import venix.hookla.types.GitlabPayloads._

class WebhookController @Inject()(
  actor: ActorRef[EventHandlerCommand],
  providerSettingsService: ProviderSettingsService,
  discordWebhookService: DiscordWebhookService
)(
    implicit executionContext: ExecutionContext
) extends BaseController {
  def endpoints = process :+: getHookInfo

  def getHookInfo: Endpoint[IO, Json] = get("process" :: path[String]) { token: String =>
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        logger.debug(s"fetched data for provider ${providerSettings.slug}")

      Ok(providerSettings.asJson)
    }
  }

  def process: Endpoint[IO, String] = post("process" :: path[String] :: jsonBody[Json] :: headersAll) { (token: String, body: Json, headers: Map[String, String]) =>
    println(body)
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        logger.debug(s"fetched data for provider ${providerSettings.slug}")

        val headerName = providerSettings.slug match {
          case "github" => Github.provider.eventHeader
          case "gitlab" => Gitlab.provider.eventHeader
        }

        headerName.fold(throw new Exception("event header name not found")) { headerName =>
          headers.get(headerName).fold(throw new Exception("event header not found")) { eventName =>
            val decoder = providerSettings.slug match {
              case "github" => githubEvents.get(eventName)
              case "gitlab" => gitlabEvents.get(eventName)
            }

            decoder.fold(throw new Exception("event not handled")) { implicit decoder =>
              decoder.decodeJson(body) match {
                case Left(e) => logger.error(e.getMessage())
                case Right(body) => discordWebhookService.getById(providerSettings.discordWebhookId) map {
                  case None => ???
                  case Some(hook) =>
                    providerSettingsService.getOptionsForProvider(providerSettings) map { options =>
                      actor ! body.toEvent(hook, options) // This is just intelliJ being shite.
                    }
                }
              }
            }
          }
        }

        Ok("success")
    }
  }
}
