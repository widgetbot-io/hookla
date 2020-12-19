package venix.hookla.types

import ackcord.data.OutgoingEmbed
import io.circe.Encoder
import io.circe.generic.semiauto._

case class OutgoingWebhookPayload(
  embeds: List[OutgoingEmbed]
)

object OutgoingWebhookPayload {
  import ackcord.data.DiscordProtocol._ // Codecs for Discord Objects.

  implicit val outgoingWebhookPayloadEncoder: Encoder[OutgoingWebhookPayload] = deriveEncoder
}