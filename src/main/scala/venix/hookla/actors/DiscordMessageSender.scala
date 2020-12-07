package venix.hookla.actors

import ackcord.data.OutgoingEmbed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.twitter.finagle.http.Response
import com.twitter.finagle.{Http, Service, http}
import com.twitter.io.Buf
import venix.hookla.types.OutgoingWebhookPayload
import io.circe.generic.auto._
import io.circe.syntax._
import venix.hookla.models.DiscordWebhook

object Discord {
  sealed trait Command

  final case class SendEmbedToDiscord(discordWebhook: DiscordWebhook, embed: OutgoingEmbed) extends Command
}

object DiscordMessageSender {
  def apply(): Behavior[Discord.Command] =
    Behaviors.setup(ctx => new DiscordMessageSenderBehaviour(ctx))

  class DiscordMessageSenderBehaviour(context: ActorContext[Discord.Command]) extends AbstractBehavior[Discord.Command](context) {
    import Discord._

    override def onMessage(e: Command): Behavior[Command] =
      e match {
        case SendEmbedToDiscord(discordWebhook, embed) =>
          val payload = OutgoingWebhookPayload(List(embed))
          sendMessageToDiscord(discordWebhook.discordWebhookId, discordWebhook.discordWebhookToken, payload)
          this
      }

    private val discordClient: Service[http.Request, http.Response] =
      Http.client.withSessionQualifier.noFailFast.withSessionQualifier.noFailureAccrual
        .withTls("discordapp.com")
        .newService("discordapp.com:443")

    private def sendMessageToDiscord(id: String, token: String, payload: OutgoingWebhookPayload) = {
      val request = http.Request(http.Method.Post, s"/api/webhooks/$id/$token")

      request.write(payload.asJson.toString())
      request.headerMap.add("Content-Type", "application/json")

      discordClient(request) onSuccess { res: Response =>
        res.reader.read() map { m =>
          val Buf.Utf8(str) = m.get
          println(str)
        }

        println("success", res)
      } onFailure { ex: Throwable =>
        println(ex)
      }
    }
  }
}