package venix.hookla.handlers.github

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types._
import venix.hookla.util.Colours

class IssueEvent(
    discordMessageService: DiscordMessageService
) extends BaseHandler[GithubIssuePayload] {
  def handleEvent(payload: GithubIssuePayload, data: HandlerData) = {
    println(s"issue $payload")
  }
}
