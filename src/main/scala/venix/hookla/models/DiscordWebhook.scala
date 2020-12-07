package venix.hookla.models

import java.util.{Date, UUID}

case class DiscordWebhook(
  id: UUID,
  userId: UUID,
  discordWebhookId: UUID,
  token: String,
    
  createdAt: Date
)
