package venix.hookla.models

import java.util.{Date, UUID}

case class ProviderSettings(
  id: UUID,
  userId: UUID,
  discordWebhookId: String,
  discordWebhookToken: String,

  createdAt: Date
)
