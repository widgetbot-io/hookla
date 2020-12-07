package venix.hookla.types

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
)