package venix.hookla.controllers

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import akka.actor.typed.ActorRef
import cats.effect._
import com.google.inject.Inject
import com.twitter.finagle._
import com.twitter.finagle.http.Response
import com.twitter.io.Buf
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import io.finch.{Endpoint, _}
import io.finch.circe._
import java.time.OffsetDateTime
import venix.hookla.actors.Discord.SendEmbedToDiscord
import venix.hookla.actors._
import venix.hookla.util.Colours

class WebhookController @Inject()(
  actor: ActorRef[EventHandlerCommand],
  discordActor: ActorRef[Discord.Command],
) extends BaseController {
  def endpoints = process

  def process: Endpoint[IO, String] = post("process" :: path[String] :: jsonBody[Json] :: headersAll) { (providerId: String, body: Json, headers: Map[String, String]) =>
    val provider: Option[Provider] = None // TODO: logic for getting provider via providerId

    provider match {
      case None => Ok("success")
      case Some(provider) =>
        val embed = OutgoingEmbed(
          description = Some("Description type beat"),
          author = Some(OutgoingEmbedAuthor("viction", None, Some("https://i.viction.dev/assets/images/avi.png"))),
          url = Some("https://github.com/widgetbot-io/hookla"),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.PUSH),
          footer = Some(OutgoingEmbedFooter("widgetbot-io/hookla:develop", Some("https://i.viction.dev/assets/images/avi.png")))
        )

        discordActor ! SendEmbedToDiscord(embed)

        actor ! Gitlab.PushEvent("test")
        actor ! Github.PushEvent("test")

        Ok("success")

    }
  }
}
