package venix.hookla.models

import java.util.UUID

case class ProviderSettings(
  id: UUID,
  userId: UUID,
  discordWebhookId: UUID,
  slug: String,
  token: String
)
