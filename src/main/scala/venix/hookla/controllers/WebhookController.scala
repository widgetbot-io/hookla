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
import venix.hookla.actors._
import venix.hookla.services.ProviderService
  
case class OutgoingWebhookPayload(
  embeds: List[OutgoingEmbed]
)

object OutgoingWebhookPayload {
  import io.circe.generic.auto._
  implicit val outgoingWebhookPayloadEncoder: Encoder[OutgoingWebhookPayload] = deriveEncoder
}

class WebhookController @Inject()(
  actor: ActorRef[EventHandlerCommand],
  providerService: ProviderService
) extends BaseController { // Codecs for Discord Objects.

  private val discordClient: Service[http.Request, http.Response] =
    Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
      .withTls("discordapp.com")
      .newService("discordapp.com:443")

  def endpoints = process

  def pathProviderId: Endpoint[IO, String] = path[String]
  def process: Endpoint[IO, String] = post("process" :: pathProviderId :: jsonBody[Json] :: headersAll) { (providerId: String, body: Json, headers: Map[String, String]) =>
    val provider = providerService.getById(providerId)

    provider match {
      case None => Ok("success")
      case Some(provider) =>
        val embed = OutgoingEmbed(
          description = Some("Description type beat"),
          author = Some(OutgoingEmbedAuthor("viction", None, Some("https://i.viction.dev/assets/images/avi.png"))),
          url = Some("https://github.com/widgetbot-io/hookla"),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(0x333333),
          footer = Some(OutgoingEmbedFooter("widgetbot-io/hookla:develop", Some("https://i.viction.dev/assets/images/avi.png")))
        )

        val payload = OutgoingWebhookPayload(embed :: Nil)
        val request = http.Request(http.Method.Post, "/api/webhooks/ID/SECRET")

        request.write(payload.asJson.toString())
        request.headerMap.add("Content-Type", "application/json")


        discordClient(request) onSuccess { res: Response =>
          res.reader.read() map { meme =>
            val Buf.Utf8(str) = meme.get
            println(str)
          }

          println("success", res)
        } onFailure { ex: Throwable =>
          println(ex)
        }

        actor ! Gitlab.PushEvent("test")
        actor ! Github.PushEvent("test")

        Ok("success")

    }
  }
}
