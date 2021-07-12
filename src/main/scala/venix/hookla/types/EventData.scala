package venix.hookla.types

import venix.hookla.models.{DiscordWebhook, EmbedOptions}

case class EventData(
    hook: DiscordWebhook,
    options: Option[EmbedOptions] = None
)
