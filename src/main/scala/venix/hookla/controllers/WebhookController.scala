package venix.hookla.controllers

import akka.actor.typed.ActorRef
import cats.effect._
import io.circe.Decoder.Result
import io.circe.{Decoder, Json}
import io.finch._
import io.finch.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.{ExecutionContext, Future}
import venix.hookla.handlers.MainHandler
import venix.hookla.handlers.github.GithubHandler
import venix.hookla.handlers.gitlab.GitlabHandler
import venix.hookla.services.{DiscordWebhookService, ProviderSettingsService}
import venix.hookla.types.{BasePayload, GithubPayload, GithubPayloads, GitlabPayload, HandlerData}
import venix.hookla.types.GithubPayloads._
import venix.hookla.types.GitlabPayloads._

class WebhookController(
    providerSettingsService: ProviderSettingsService,
    discordWebhookService: DiscordWebhookService,
    mainHandler: MainHandler
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

  def process: Endpoint[IO, String] = post(
    "process" :: path[String] :: jsonBody[Json] :: headersAll
  ) { (token: String, body: Json, headers: Map[String, String]) =>
    println(body)
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        logger.debug(s"fetched data for provider ${providerSettings.slug}")

        val headerName = providerSettings.slug match {
          case "github" => GithubHandler.provider.eventHeader
          case "gitlab" => GitlabHandler.provider.eventHeader
        }

        headerName match {
          case None => BadRequest(new Exception("event header name not found"))
          case Some(headerName) =>
            headers.get(headerName).fold(throw new Exception("event header not found")) {
              eventName =>
                println(s"got header: $headerName -> $eventName")

                val decoder = providerSettings.slug match {
                  case "github" => githubEvents.get(eventName)
                  case "gitlab" => gitlabEvents.get(eventName)
                }

                decoder match {
                  case None => BadRequest(new Exception("Invalid event"))
                  case Some(decoder) =>
                    decoder.decodeJson(body) match {
                      case Left(e) =>
                        println("----ERROR OCCURRED WHILE PARSING BODY----")
                        println(body)
                        println(e)
                        InternalServerError(new Exception("An error occurred parsing your event!"))
                      case Right(body) =>
                        discordWebhookService.getById(providerSettings.discordWebhookId) flatMap {
                          case None => ???
                          case Some(hook) =>
                            providerSettingsService.getOptionsForProvider(providerSettings) map { options =>
                              mainHandler.handle(body, HandlerData(hook, options))
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
