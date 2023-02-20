package venix.hookla.controllers

import cats.effect._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.finch._
import io.finch.circe._
import scala.concurrent.ExecutionContext
import venix.hookla.handlers.MainHandler
import venix.hookla.handlers.github.GithubHandler
import venix.hookla.handlers.gitlab.GitlabHandler
import venix.hookla.handlers.sonarr.SonarrHandler
import venix.hookla.services.{DiscordWebhookService, ProviderSettingsService}
import venix.hookla.types.EventData
import venix.hookla.types.providers.GithubPayloads._
import venix.hookla.types.providers.GitlabPayloads._
import venix.hookla.types.providers.SonarrPayloads._

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
  ) { (token: String, body: Json, rawHeaders: Map[String, String]) =>
    val headers = rawHeaders.map { case (k, v) => (k.toLowerCase, v) }

    println(body)
    println(headers)
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        logger.debug(s"fetched data for provider ${providerSettings.slug}")

        val provider = providerSettings.slug match {
          case "github" => Some(GithubHandler.provider)
          case "gitlab" => Some(GitlabHandler.provider)
          case "sonarr" => Some(SonarrHandler.provider)
          case _        => None
        }

        provider match {
          case None => BadRequest(new Exception("event header name not found"))
          case Some(provider) =>
            val eventName: String =
              if (provider.isBody)
                body.hcursor.get[String](provider.eventKey) match {
                  case Left(err) =>
                    println(s"Provider ${provider.name} has eventKey ${provider.eventKey} and isBody but eventKey can't be found in the JSON passed.")
                    println(err)
                    throw new Exception("internal server error")
                  case Right(v) => v
                }
              else headers.get(provider.eventKey.toLowerCase).fold(throw new Exception("event header not found"))(identity)

            val decoder = providerSettings.slug match {
              case "github" => githubEvents.get(eventName)
              case "gitlab" => gitlabEvents.get(eventName)
              case "sonarr" => sonarrEvents.get(eventName)
            }

            decoder match {
              case None => BadRequest(new Exception("invalid event"))
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
                        providerSettingsService.getOptionsForProvider(providerSettings) map { options => mainHandler.handle(body, EventData(hook, options)) }
                    }
                }
            }
        }
        Ok("success")
    }
  }

}
