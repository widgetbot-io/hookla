package venix.hookla.handlers

import venix.hookla.handlers.github.GithubHandler
import venix.hookla.handlers.gitlab.GitlabHandler
import venix.hookla.services.DiscordMessageService
import venix.hookla.types.providers.{GithubPayload, GitlabPayload}
import venix.hookla.types.{BasePayload, EventData, Provider}

class MainHandler(
    discordMessageService: DiscordMessageService
) extends BaseHandler[BasePayload] {
  import com.softwaremill.macwire._

  override def handle(payload: BasePayload, data: EventData): Unit = {
    lazy val githubHandler = wire[GithubHandler]
    lazy val gitlabHandler = wire[GitlabHandler]

    payload match {
      case payload: GithubPayload => githubHandler.handle(payload, data)
      case payload: GitlabPayload => gitlabHandler.handle(payload, data)
      case _ =>
        println("This provider is unhandled, you need to add an entry into MainHandler.")
        ???
    }
  }
}
