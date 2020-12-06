package venix.hookla.controllers

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import cats.effect._
import io.finch.Endpoint
import com.twitter.finagle._
import com.twitter.finagle.http.Response
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import io.finch.circe._
import io.finch._
import java.time.OffsetDateTime
import javax.inject.Inject
import venix.hookla.services.ProviderService

case class OutgoingWebhookPayload(
    embeds: List[OutgoingEmbed]
)

class WebhookController @Inject()(
  providerService: ProviderService
) extends BaseController {
  private val discordClient: Service[http.Request, http.Response] =
    Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
      .withTls("0b6e37a53a23.ngrok.io")
      .newService("0b6e37a53a23.ngrok.io" + ":443")


  def endpoints: Endpoint[IO, String] = process

  def pathProviderId: Endpoint[IO, String] = path[String]

  def process: Endpoint[IO, String] = post("process" :: pathProviderId :: jsonBody[Json] :: headersAll) { (providerId: String, body: Json, headers: Map[String, String]) =>
    val provider = providerService.getById(providerId)

    println("hit")

    provider match {
      case None => Ok("success")
      case Some(provider) => {

        val embed = OutgoingEmbed(
          author = Some(OutgoingEmbedAuthor("viction", None, Some("https://i.viction.dev/assets/images/avi.png"))),
          url = Some("https://github.com/widgetbot-io/hookla"),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(0x333333),
          footer = Some(OutgoingEmbedFooter("widgetbot-io/hookla:master", Some("https://i.viction.dev/assets/images/avi.png")))
        )

        val payload = OutgoingWebhookPayload(embed :: Nil)

        val request = http.Request(http.Method.Post, "memes")

        request.write(payload.asJson.toString())

        println(payload.asJson.toString())

        discordClient(request) onSuccess { res: Response =>
        res.reader.read() map { meme =>
          val Buf.Utf8(str) = meme.get

          println(str)
        }

          println("success", res)
        } onFailure { ex: Throwable =>
          println(ex)
        }

        Ok("success but actually a success")
      }
    }
  }
}
