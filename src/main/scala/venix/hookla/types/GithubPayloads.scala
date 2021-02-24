package venix.hookla.types

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait GithubPayload extends BasePayload

case class GithubPusher(
    name: String,
    email: String
)

case class GithubCommit(
    message: String,
    author: GithubPusher
)

case class GithubSender(
    avatar_url: String
)

case class GithubRepository(
    name: String,
    html_url: String,
    full_name: String
)

case class GithubCheckRun(

)

case class GithubPushPayload(
    ref: String,
    before: String,
    after: String,
    commits: Seq[GithubCommit],
    pusher: GithubPusher,
    sender: GithubSender,
    repository: GithubRepository
) extends GithubPayload

case class GithubCheckRunPayload(
    action: String
) extends GithubPayload

case class GithubIssuePayload(
    action: String
) extends GithubPayload

object GithubPayloads {
  val githubEvents: Map[String, Decoder[GithubPayload]] = Map(
    "push"   -> Decoder[GithubPushPayload].widen,
    "issues" -> Decoder[GithubIssuePayload].widen
  )

  implicit val encodeGithubEvent: Encoder[GithubPayload] = Encoder.instance {
    case pushPayload: GithubPushPayload   => pushPayload.asJson
    case issuePayload: GithubIssuePayload => issuePayload.asJson
  }
}
