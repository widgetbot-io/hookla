package venix.hookla.controllers

import akka.actor.typed.ActorRef
import cats.effect._
import com.google.inject.Inject
import io.circe.Decoder.Result
import cats.syntax.functor._
import io.circe.{Decoder, Json}
import io.finch._
import io.finch.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext
import venix.hookla.actors._
import venix.hookla.services.{DiscordWebhookService, ProviderSettingsService}
import venix.hookla.types.{BasePayload, GithubPayload, GitlabPayload}
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


        val decoded = providerSettings.slug match {
          case "github" => body.as[GithubPayload]
          case "gitlab" => body.as[GitlabPayload]
        }

        decoded match {
          case Left(err) => logger.error(err.getMessage())
          case Right(body) =>
            discordWebhookService.getById(providerSettings.discordWebhookId) map {
              case None => ???
              case Some(hook) =>
                providerSettingsService.getOptionsForProvider(providerSettings) map { options =>
                  actor ! body.toEvent(hook, options)
                }
            }
        }

        Ok("success")
    }
  }
}
