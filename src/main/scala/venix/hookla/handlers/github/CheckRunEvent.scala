package venix.hookla.handlers.github

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types._
import venix.hookla.types.providers.{GithubCheckRunAction, GithubCheckRunPayload}
import venix.hookla.util.Colours

class CheckRunEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GithubCheckRunPayload] {
  def handleEvent(payload: GithubCheckRunPayload, data: EventData) = {

    payload.action match {
      case GithubCheckRunAction.Created =>
        if (List("deploy-", "deploy ").exists(payload.check_run.name.toLowerCase.startsWith)) {
          val environment = payload.check_run.name.substring(7)

          if (environment.nonEmpty) {
            val embed = makeJobEmbed(
              payload = payload,
              colour = Colours.RUNNING,
              description = s"Version ${payload.check_run.head_branch} is deploying to ${environment}..."
            )

            discordMessageService.sendMessageToDiscord(data.hook, embed)
          }
        }
      case GithubCheckRunAction.Completed =>
      case GithubCheckRunAction.ReRequested =>
      case GithubCheckRunAction.RequestedAction =>
    }
  }

  def makeJobEmbed(payload: GithubCheckRunPayload, colour: Int, description: String) = OutgoingEmbed(
    author = Some(OutgoingEmbedAuthor(payload.sender.login, None, Some(payload.sender.avatar_url))),
    url = Some(payload.check_run.html_url),
    timestamp = Some(OffsetDateTime.now()),
    footer = Some(
      OutgoingEmbedFooter(
        s"${payload.repository.full_name}:${payload.check_run.head_sha.substring(0, 7)}",
        Some(GithubHandler.provider.logo)
      )
    ),
    color = Some(colour),
    description = Some(description)
  )
}
