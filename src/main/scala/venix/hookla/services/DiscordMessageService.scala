package venix.hookla.services

import ackcord.data.OutgoingEmbed
import com.twitter.finagle.{Http, Service, http}
import com.twitter.finagle.http.Response
import com.twitter.io.Buf
import com.twitter.util.Future
import io.circe.syntax._
import venix.hookla.models.DiscordWebhook
import venix.hookla.types.OutgoingWebhookPayload

class DiscordMessageService {
  private val discordClient: Service[http.Request, http.Response] =
    Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
      .withTls("discord.com")
      .newService("discord.com:443")

  def sendMessageToDiscord(discordWebhook: DiscordWebhook, embed: OutgoingEmbed): Future[Response] = {
    val payload = OutgoingWebhookPayload(List(embed))
    val request =
      http.Request(http.Method.Post, s"/api/webhooks/${discordWebhook.discordWebhookId}/${discordWebhook.discordWebhookToken}")

    println("Sending following JSON to Discord...")
    println(payload.asJson)

    request.write(payload.asJson.toString())
    request.headerMap.add("Content-Type", "application/json")

    discordClient(request) onSuccess { res: Response =>
      res.reader.read() map { m =>
        val Buf.Utf8(str) = m.get
        println(str)
      }

      println("success", res)
    } onFailure { ex: Throwable => println(ex) }
  }

}
