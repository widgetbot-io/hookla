package venix.hookla.providers.gitlab

import venix.hookla.providers.BaseProvider

class GitlabProvider extends BaseProvider {
  def id = "gitlab"
  def name = "Gitlab"
  def logo = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/GitLab_Logo.svg/1108px-GitLab_Logo.svg.png"

  override def eventHeader = Some("X-Gitlab-Event")
  override def tokenHeader = Some("X-Gitlab-Token")

  def events = Nil
}
