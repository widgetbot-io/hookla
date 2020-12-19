package venix.hookla.types

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import venix.hookla.actors.Github
import venix.hookla.models.{DiscordWebhook, EmbedOptions}

sealed trait GithubPayload extends BasePayload[Github.Event]

case class GithubPusher (
  name: String,
  email: String
)

case class GithubCommit (
  message: String,
  author: GithubPusher
)

case class GithubSender (
    avatar_url: String
)

case class GithubRepository (
  name: String,
  html_url: String,
  full_name: String
)

case class GithubPushPayload (
  ref: String,
  before: String,
  after: String,

  commits: Seq[GithubCommit],
  pusher: GithubPusher,
  sender: GithubSender,
  repository: GithubRepository
) extends GithubPayload {
  override def toEvent(discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions]) = Github.PushEvent(this, discordWebhook, embedOptions)
}

case class GithubIssuePayload(
  action: String
) extends GithubPayload {
  override def toEvent(discordWebhook: DiscordWebhook, embedOptions: Option[EmbedOptions]) = Github.IssueEvent(this, discordWebhook, embedOptions)
}

object GithubPayloads {
  implicit val encodeGithubEvent: Encoder[GithubPayload] = Encoder.instance {
    case pushPayload: GithubPushPayload => pushPayload.asJson
    case issuePayload: GithubIssuePayload => issuePayload.asJson
  }

  implicit val decodeGithubEvent: Decoder[GithubPayload] =
    List[Decoder[GithubPayload]](
      Decoder[GithubPushPayload].widen,
      Decoder[GithubIssuePayload].widen,
    ).reduceLeft(_ or _)
}