package venix.hookla.types

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import venix.hookla.actors.Gitlab
import venix.hookla.models.DiscordWebhook

sealed trait GitlabPayload {
  def toEvent(discordWebhook: DiscordWebhook): Gitlab.Event
}

case class GitlabAuthor(
    name: String,
    email: String
)

case class GitlabProject(
    name: String,
    path_with_namespace: String,
    web_url: String,
    description: String,
    url: String
)

case class GitlabRepository(
    name: String,
    url: String,
    description: String,
    homepage: String
)

case class GitlabCommit(
    id: String,
    message: String,
    timestamp: String,
    url: String,
    author: GitlabAuthor,
    added: List[String],
    modified: List[String],
    removed: List[String]
)

case class GitlabMergeRequest(
    id: Int,
    iid: Int
)

case class GitlabObjectAttributes(
    noteable_type: String,
    note: String
)

case class GitlabUser(
    name: String,
    username: String,
    avatar_url: String
)

case class GitlabPushPayload(
    object_kind: String,
    before: String,
    after: String,
    ref: String,
    checkout_sha: String,
    user_id: Int,
    user_name: String,
    user_email: String,
    user_avatar: String,
    project_id: Int,
    project: GitlabProject,
    repository: GitlabRepository,
    commits: List[GitlabCommit],
    total_commits_count: Int
) extends GitlabPayload {
  override def toEvent(discordWebhook: DiscordWebhook): Gitlab.Event = Gitlab.PushEvent(this, discordWebhook)
}

case class GitlabNotePayload(
    object_kind: String,
    user: GitlabUser,
    project_id: Int,
    project: GitlabProject,
    repository: GitlabRepository,
    object_attributes: GitlabObjectAttributes,
    commit: Option[GitlabCommit],
    merge_request: Option[GitlabMergeRequest]
) extends GitlabPayload {
  override def toEvent(discordWebhook: DiscordWebhook): Gitlab.Event = Gitlab.NoteEvent(this, discordWebhook)
}

case class GitlabIssuePayload(
    action: String
) extends GitlabPayload {
  override def toEvent(discordWebhook: DiscordWebhook): Gitlab.Event = Gitlab.IssueEvent(this, discordWebhook)
}

object GitlabPayloads {
  implicit val encodeGitlabEvent: Encoder[GitlabPayload] = Encoder.instance {
    case notePayload: GitlabNotePayload   => notePayload.asJson
    case pushPayload: GitlabPushPayload   => pushPayload.asJson
    case issuePayload: GitlabIssuePayload => issuePayload.asJson
  }

  implicit val decodeGitlabEvent: Decoder[GitlabPayload] =
    List[Decoder[GitlabPayload]](
      Decoder[GitlabNotePayload].widen,
//      Decoder[GitlabPushPayload].widen,
//      Decoder[GitlabIssuePayload].widen,
    ).reduceLeft(_ or _)
}
