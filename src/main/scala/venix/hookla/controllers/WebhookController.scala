package venix.hookla.controllers

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import cats.effect._
import io.finch.Endpoint
import com.twitter.finagle._
import com.twitter.finagle.http.Response
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray
import io.circe.{Codec, Encoder, Json, derivation}
import io.circe.syntax._
import io.finch.circe._
import io.finch._
import java.time.OffsetDateTime
import javax.inject.Inject
import venix.hookla.services.ProviderService
import io.circe.generic.semiauto._

case class OutgoingWebhookPayload(
    embeds: List[OutgoingEmbed]
)

object OutgoingWebhookPayload {
  implicit val outgoingWebhookPayloadEncoder: Encoder[OutgoingWebhookPayload] = deriveEncoder
}

class WebhookController @Inject()(
  providerService: ProviderService
) extends BaseController {
  import ackcord.data.DiscordProtocol._ // Codecs for Discord Objects.

  private val discordClient: Service[http.Request, http.Response] =
    Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
      .withTls("discordapp.com")
      .newService("discordapp.com:443")

  def endpoints: Endpoint[IO, String] = process

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

        Ok("success")
    }
  }
}
