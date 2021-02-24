package venix.hookla.handlers.gitlab

import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.{GitlabIssuePayload, GitlabTagPushPayload, HandlerData}

class IssueEvent(
  discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabIssuePayload] {
  def handleEvent(payload: GitlabIssuePayload, data: HandlerData) = {
    ???
  }
}