package venix.hookla.types

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import venix.hookla.actors.Github
import venix.hookla.models.ProviderSettings

sealed trait GithubPayload {
  def toEvent(providerSettings: ProviderSettings): Github.Event
}

case class GithubCommit (
  message: String
)

case class GithubPusher (
  name: String,
  email: String
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
  override def toEvent(providerSettings: ProviderSettings): Github.Event = Github.PushEvent(this, providerSettings)
}

case class GithubIssuePayload(
  action: String
) extends GithubPayload {
  override def toEvent(providerSettings: ProviderSettings): Github.Event = ???
}

object GithubPayloads {
  implicit val encodeEvent: Encoder[GithubPayload] = Encoder.instance {
    case pushPayload: GithubPushPayload => pushPayload.asJson
    case issuePayload: GithubIssuePayload => issuePayload.asJson
  }

  implicit val decodeEvent: Decoder[GithubPayload] =
    List[Decoder[GithubPayload]](
      Decoder[GithubPushPayload].widen,
      Decoder[GithubIssuePayload].widen,
    ).reduceLeft(_ or _)
}