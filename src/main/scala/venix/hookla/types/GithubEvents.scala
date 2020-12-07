package venix.hookla.types

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait GithubPayload

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
  html_url: String
)

case class GithubPushPayload (
  ref: String,
  before: String,
  after: String,

  commits: Seq[GithubCommit],
  pusher: GithubPusher,
  sender: GithubSender,
  repository: GithubRepository
) extends GithubPayload

case class GithubIssuePayload(
  action: String
) extends GithubPayload

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