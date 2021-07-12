package venix.hookla.handlers.github

import venix.hookla.handlers.BaseHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types._
import venix.hookla.types.providers.{GithubCheckRunPayload, GithubIssuePayload, GithubPayload, GithubPushPayload}

class GithubHandler(
    discordMessageService: DiscordMessageService
) extends BaseHandler[GithubPayload] {
  import com.softwaremill.macwire._

  def handle(payload: GithubPayload, data: HandlerData) = payload match {
    case payload: GithubPushPayload     => wire[PushEvent].handleEvent(payload, data)
    case payload: GithubIssuePayload    => wire[IssueEvent].handleEvent(payload, data)
    case payload: GithubCheckRunPayload => wire[CheckRunEvent].handleEvent(payload, data)
    case _ =>
      println("You have an unhandled GitHub payload, you need to add an try to GithubHandler.")
      ???
  }
}

object GithubHandler {
  val provider = Provider(
    "github",
    "GitHub",
    "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png",
    Some("X-GitHub-Event")
  )
}
