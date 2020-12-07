package venix.hookla.types

import ackcord.data.OutgoingEmbed
import io.circe.Encoder

case class OutgoingWebhookPayload(
  embeds: List[OutgoingEmbed]
)

object OutgoingWebhookPayload {
  import ackcord.data.DiscordProtocol._ // Codecs for Discord Objects.
  import io.circe.generic.semiauto._

  implicit val outgoingWebhookPayloadEncoder: Encoder[OutgoingWebhookPayload] = deriveEncoder
}