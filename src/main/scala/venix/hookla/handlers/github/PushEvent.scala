package venix.hookla.handlers.github

import ackcord.data.{EmbedField, OutgoingEmbed, OutgoingEmbedAuthor, OutgoingEmbedFooter}
import java.time.OffsetDateTime
import scala.concurrent.Future
import venix.hookla.handlers.BaseEvent
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.EventData
import venix.hookla.types.providers.{GithubCommit, GithubPushPayload}
import venix.hookla.util.Colours

class PushEvent(
    discordMessageService: DiscordMessageService
) extends BaseEvent[GithubPushPayload] {
  def handleEvent(payload: GithubPushPayload, data: EventData) = {
    val branchName                             = payload.ref.split('/').drop(2).mkString("/")
    val groupedCommits: Seq[Seq[GithubCommit]] = payload.commits.groupBy(_.author.email).toSeq.map(_._2)

    groupedCommits.length match {
      case 1 =>
        val description =
          groupedCommits.head
            .map(c => formatCommit(c.message, groupedCommits.head.length, c.url, data.options))
            .mkString("\n")
            .replaceAll("/\n$/", "")

        discordMessageService.sendMessageToDiscord(
          data.hook,
          OutgoingEmbed(
            description = Some(description),
            author = Some(OutgoingEmbedAuthor(payload.pusher.name, None, Some(payload.sender.avatar_url))),
            url = Some(payload.repository.html_url),
            timestamp = Some(OffsetDateTime.now()),
            color = Some(Colours.PUSH),
            footer = Some(OutgoingEmbedFooter(s"${payload.repository.full_name}:$branchName", Some(GithubHandler.provider.logo)))
          )
        )
      case x if x > 1 =>
        val fields =
          groupedCommits.map { d =>
            EmbedField(
              s"Commits from ${d.head.author.name}",
              d.map(c => formatCommit(c.message, d.length, c.url, data.options)).mkString("\n").replaceAll("/\n$/", ""),
              Some(false)
            )
          }

        discordMessageService.sendMessageToDiscord(
          data.hook,
          OutgoingEmbed(
            author = Some(OutgoingEmbedAuthor(payload.pusher.name, None, Some(payload.sender.avatar_url))),
            url = Some(payload.repository.html_url),
            timestamp = Some(OffsetDateTime.now()),
            fields = fields,
            color = Some(Colours.PUSH),
            footer = Some(OutgoingEmbedFooter(s"${payload.repository.full_name}:$branchName", Some(GithubHandler.provider.logo)))
          )
        )
      case _ =>
        println("_")
    }
  }
}
