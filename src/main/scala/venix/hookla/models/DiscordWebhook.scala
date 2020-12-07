package venix.hookla.models

import java.util.UUID

case class DiscordWebhook(
  id: UUID,
  userId: UUID,
  discordWebhookId: String,
  discordWebhookToken: String
)
