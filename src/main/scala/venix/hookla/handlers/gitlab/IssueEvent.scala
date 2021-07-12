package venix.hookla.handlers.gitlab

import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.GitlabIssuePayload

class IssueEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabIssuePayload] {
  def handleEvent(payload: GitlabIssuePayload, data: EventData) = {
    ???
  }
}