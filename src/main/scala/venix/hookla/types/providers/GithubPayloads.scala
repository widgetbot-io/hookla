package venix.hookla.types.providers

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import venix.hookla.types.BasePayload

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
    login: String,
    avatar_url: String,
    html_url: String
)

case class GithubOwner(
    login: String
)

case class GithubRepository(
    name: String,
    html_url: String,
    full_name: String,
    owner: GithubOwner
)

case class GithubCheckRun(
    id: Int,
    head_branch: String,
    html_url: String,
    head_sha: String,
    status: GithubCheckRunStatus,
    conclusion: Option[GithubCheckRunConclusion],
    name: String
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
    action: GithubCheckRunAction,
    check_run: GithubCheckRun,
    repository: GithubRepository,
    sender: GithubSender
) extends GithubPayload

case class GithubIssuePayload(
    action: String
) extends GithubPayload

object GithubPayloads {
  val githubEvents: Map[String, Decoder[GithubPayload]] = Map(
    "push"      -> Decoder[GithubPushPayload].widen,
    "issues"    -> Decoder[GithubIssuePayload].widen,
    "check_run" -> Decoder[GithubCheckRunPayload].widen
  )

  implicit val encodeGithubEvent: Encoder[GithubPayload] = Encoder.instance {
    case payload: GithubPushPayload     => payload.asJson
    case payload: GithubIssuePayload    => payload.asJson
    case payload: GithubCheckRunPayload => payload.asJson
  }
}
