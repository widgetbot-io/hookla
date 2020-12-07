package venix.hookla.models

import java.util.{Date, UUID}

case class ProviderSettings(
  id: UUID,
  userId: UUID,
  discordWebhookId: UUID,
  slug: String,
  token: String,

  createdAt: Date
)
