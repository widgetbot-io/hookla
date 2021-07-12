package venix.hookla.handlers.gitlab

import venix.hookla.handlers.{BaseEvent, BaseHandler}
import venix.hookla.services.DiscordMessageService
import venix.hookla.types._
import venix.hookla.types.providers.{GitlabIssuePayload, GitlabJobPayload, GitlabNotePayload, GitlabPayload, GitlabPushPayload, GitlabTagPushPayload}

class GitlabHandler(
    discordMessageService: DiscordMessageService
) extends BaseHandler[GitlabPayload] {
  import com.softwaremill.macwire._

  def handle(payload: GitlabPayload, data: EventData) = payload match {
    case payload: GitlabPushPayload    => wire[PushEvent].handleEvent(payload, data)
    case payload: GitlabTagPushPayload => wire[TagEvent].handleEvent(payload, data)
    case payload: GitlabNotePayload    => wire[NoteEvent].handleEvent(payload, data)
    case payload: GitlabIssuePayload   => wire[IssueEvent].handleEvent(payload, data)
    case payload: GitlabJobPayload     => wire[JobEvent].handleEvent(payload, data)
    case _ =>
      println("You have an unhandled GitLab payload, you need to add an try to GitlabHandler.")
      ???
  }
}

object GitlabHandler {
  val provider = Provider(
    "gitlab",
    "Gitlab",
    "https://about.gitlab.com/images/press/logo/png/gitlab-icon-rgb.png",
    "X-Gitlab-Event"
  )
}
