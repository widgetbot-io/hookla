package venix.hookla.types

import venix.hookla.actors.EventHandlerCommand
import venix.hookla.models.DiscordWebhook

trait BasePayload[E <: EventHandlerCommand] {
  def toEvent(discordWebhook: DiscordWebhook): E
}
