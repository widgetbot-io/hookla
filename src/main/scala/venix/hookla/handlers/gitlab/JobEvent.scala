package venix.hookla.handlers.gitlab

import ackcord.data.{OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.handlers.github.GithubHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.GitlabJobPayload
import venix.hookla.util.Colours

class JobEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabJobPayload] {
  def handleEvent(payload: GitlabJobPayload, data: EventData) = {

    payload.build_status match {
      case "failed" =>
        if (!payload.build_allow_failure) {
          discordMessageService.sendMessageToDiscord(
            data.hook,
            makeJobEmbed(
              payload = payload,
              colour = Colours.FAILED,
              description = "The job has failed."
            )
          )
        }

      case "canceled" =>
        discordMessageService.sendMessageToDiscord(
          data.hook,
          makeJobEmbed(
            payload = payload,
            colour = Colours.CANCELED,
            description = "The job has has been canceled."
          )
        )

      case "running" =>
        if (payload.build_name.startsWith("deploy-")) {
          val environment = payload.build_name.substring(7)

          if (environment.nonEmpty) {
            val embed = if (payload.tag) {
              makeJobEmbed(
                payload = payload,
                colour = Colours.RUNNING,
                description = s"Version ${payload.ref} is deploying to ${environment}..."
              )
            } else {
              makeJobEmbed(
                payload = payload,
                colour = Colours.CANCELED,
                description = s"Deploying latest commit to ${environment}..."
              )
            }

            discordMessageService.sendMessageToDiscord(data.hook, embed)
          }
        }

      case "success" =>
        if (payload.build_name.startsWith("deploy-")) {
          val environment = payload.build_name.substring(7)

          if (environment.nonEmpty) {
            val embed = if (payload.tag) {
              makeJobEmbed(
                payload = payload,
                colour = Colours.RUNNING,
                description = s"Version ${payload.ref} has been deployed to ${environment}."
              )
            } else {
              makeJobEmbed(
                payload = payload,
                colour = Colours.CANCELED,
                description = s"Deployed latest commit to ${environment}."
              )
            }

            discordMessageService.sendMessageToDiscord(data.hook, embed)
          }
        }
    }
  }

  def makeJobEmbed(payload: GitlabJobPayload, colour: Int, description: String) = OutgoingEmbed(
    author = Some(OutgoingEmbedAuthor(payload.user.name, None, Some(payload.user.avatar_url))),
    url = Some(s"${payload.repository.homepage}/-/jobs/${payload.build_id}"),
    timestamp = Some(OffsetDateTime.now()),
    footer = Some(
      OutgoingEmbedFooter(
        s"${payload.repository.homepage.split("/").drop(3).mkString("/")}:${payload.ref}",
        Some(GitlabHandler.provider.logo)
      )
    ),
    color = Some(colour),
    description = Some(description)
  )
}
