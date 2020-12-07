package venix.hookla.controllers

import akka.actor.typed.ActorRef
import cats.effect._
import com.google.inject.Inject
import io.circe.Json
import io.finch._
import io.finch.circe._
import scala.concurrent.ExecutionContext
import venix.hookla.actors._
import venix.hookla.services.ProviderSettingsService
import venix.hookla.types.GithubPayload
import venix.hookla.types.GithubPayloads._

class WebhookController @Inject()(
  actor: ActorRef[EventHandlerCommand],
  providerSettingsService: ProviderSettingsService
)(
    implicit executionContext: ExecutionContext
) extends BaseController {
  def endpoints = process

  def process: Endpoint[IO, String] = post("process" :: path[String] :: jsonBody[Json] :: headersAll) { (token: String, body: Json, headers: Map[String, String]) =>
    providerSettingsService.getByToken(token) map {
      case None => Unauthorized(new Exception("invalid token"))
      case Some(providerSettings) =>
        println(s"fetched data for provider ${providerSettings.slug}")

        body.as[GithubPayload] match {
          case Left(error) => println(error)
          case Right(value) =>
            actor ! value.toEvent(providerSettings)
        }

//        discordActor ! SendEmbedToDiscord(OutgoingEmbed(
//          description = Some("Description type beat"),
//          author = Some(OutgoingEmbedAuthor("viction", None, Some("https://i.viction.dev/assets/images/avi.png"))),
//          url = Some("https://github.com/widgetbot-io/hookla"),
//          timestamp = Some(OffsetDateTime.now()),
//          color = Some(Colours.PUSH),
//          footer = Some(OutgoingEmbedFooter("widgetbot-io/hookla:develop", Some("https://i.viction.dev/assets/images/avi.png")))
//        ))

        Ok("success")
    }
  }
}
