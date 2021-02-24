package venix.hookla.types

import venix.hookla.models.{DiscordWebhook, EmbedOptions}

case class HandlerData( // TODO: Could do with a better name
    hook: DiscordWebhook,
    options: Option[EmbedOptions] = None
)
