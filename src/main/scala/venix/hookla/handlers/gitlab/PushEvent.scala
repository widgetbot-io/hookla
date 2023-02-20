package venix.hookla.handlers.gitlab

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import venix.hookla.handlers.BaseEvent
import venix.hookla.models.DiscordWebhook
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.GitlabPushPayload
import venix.hookla.util.Colours

class PushEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GitlabPushPayload] {
  def handleEvent(payload: GitlabPushPayload, data: EventData) = {
    val branchName = getBranchFromRef(payload.ref)
    if (!isPrivateBranch(branchName)) {
      val groupedCommits = payload.commits.groupBy(_.author.email).toList.map(_._2)
      handleBranches(payload, data.hook)

      groupedCommits.length match {
        case 1 =>
          val description =
            groupedCommits.head
              .map(c => formatCommit(c.message, groupedCommits.head.length, c.url, data.options))
              .mkString("\n")

          discordMessageService.sendMessageToDiscord(
            data.hook,
            OutgoingEmbed(
              description = Some(description),
              author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
              url = Some(payload.project.web_url),
              timestamp = Some(OffsetDateTime.now()),
              color = Some(Colours.PUSH),
              footer = Some(
                OutgoingEmbedFooter(
                  s"${payload.project.path_with_namespace}:$branchName",
                  Some(GitlabHandler.provider.logo)
                )
              )
            )
          )

        case x if x > 1 =>
          val fields =
            groupedCommits.map { d =>
              EmbedField(
                s"Commits from ${d.head.author.name}",
                d.map(c => formatCommit(c.message, d.length, c.url, data.options)).mkString("\n"),
                Some(false)
              )
            }

          discordMessageService.sendMessageToDiscord(
            data.hook,
            OutgoingEmbed(
              author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
              url = Some(payload.project.web_url),
              timestamp = Some(OffsetDateTime.now()),
              fields = fields,
              color = Some(Colours.PUSH),
              footer = Some(
                OutgoingEmbedFooter(
                  s"${payload.project.path_with_namespace}:$branchName",
                  Some(GitlabHandler.provider.logo)
                )
              )
            )
          )
        case _ =>
          println("_")
      }
    }
  }

  def handleBranches(payload: GitlabPushPayload, discordWebhook: DiscordWebhook) = {
    val refName = getBranchFromRef(payload.ref)

    if (payload.before == "0000000000000000000000000000000000000000") { // Created
      discordMessageService.sendMessageToDiscord(
        discordWebhook,
        OutgoingEmbed(
          description = Some(s"Branch created: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.CREATED),
          footer = Some(
            OutgoingEmbedFooter(
              s"${payload.project.path_with_namespace}:$refName",
              Some(GitlabHandler.provider.logo)
            )
          )
        )
      )
    } else if (payload.after == "0000000000000000000000000000000000000000") { // Deleted
      discordMessageService.sendMessageToDiscord(
        discordWebhook,
        OutgoingEmbed(
          description = Some(s"Branch deleted: $refName"),
          author = Some(OutgoingEmbedAuthor(payload.user_name, None, Some(payload.user_avatar))),
          url = Some(payload.project.web_url),
          timestamp = Some(OffsetDateTime.now()),
          color = Some(Colours.DELETED),
          footer = Some(
            OutgoingEmbedFooter(
              s"${payload.project.path_with_namespace}:$refName",
              Some(GitlabHandler.provider.logo)
            )
          )
        )
      )
    }
  }
}
