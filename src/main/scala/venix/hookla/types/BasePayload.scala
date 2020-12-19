package venix.hookla.types

import venix.hookla.actors.EventHandlerCommand
import venix.hookla.models.{DiscordWebhook, EmbedOptions}

trait BasePayload[E <: EventHandlerCommand] {
  def toEvent(discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions]): E
}
