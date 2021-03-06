package venix.hookla.types.providers

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import venix.hookla.types.BasePayload

sealed trait GitlabPayload extends BasePayload

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
    note: String,
    url: String
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
) extends GitlabPayload

case class GitlabTagPushPayload(
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
) extends GitlabPayload

case class GitlabNotePayload(
    object_kind: String,
    user: GitlabUser,
    project_id: Int,
    project: GitlabProject,
    repository: GitlabRepository,
    object_attributes: GitlabObjectAttributes,
    commit: Option[GitlabCommit],
    merge_request: Option[GitlabMergeRequest]
) extends GitlabPayload

case class GitlabIssuePayload(
    action: String
) extends GitlabPayload

case class GitlabJobPayload(
    object_kind: String,
    ref: String,
    tag: Boolean,
    before_sha: String,
    sha: String,
    build_id: Int,
    build_name: String,
    build_stage: String,
    build_status: String,
    build_started_at: String,
    build_finished_at: Option[String],
    build_duration: Float,
    build_allow_failure: Boolean,
    project_id: Int,
    project_name: String,
    user: GitlabUser,
    repository: GitlabRepository
) extends GitlabPayload

object GitlabPayloads {
  val gitlabEvents: Map[String, Decoder[GitlabPayload]] = Map(
    "Push Hook"     -> Decoder[GitlabPushPayload].widen,
    "Tag Push Hook" -> Decoder[GitlabTagPushPayload].widen,
    "Note Hook"     -> Decoder[GitlabNotePayload].widen,
    "Issue Hook"    -> Decoder[GitlabIssuePayload].widen,
    "Job Hook"      -> Decoder[GitlabJobPayload].widen,
    "Build Hook"    -> Decoder[GitlabJobPayload].widen
  )

  implicit val encodeGitlabEvent: Encoder[GitlabPayload] = Encoder.instance {
    case pushPayload: GitlabPushPayload       => pushPayload.asJson
    case tagPushPayload: GitlabTagPushPayload => tagPushPayload.asJson
    case notePayload: GitlabNotePayload       => notePayload.asJson
    case issuePayload: GitlabIssuePayload     => issuePayload.asJson
    case jobPayload: GitlabJobPayload         => jobPayload.asJson
  }
}
