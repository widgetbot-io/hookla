package venix.hookla.sources.github

import venix.hookla.Result
import venix.hookla.sources.{GithubSourceEventHandler, SourceHandler}
import zio.ZIO
import zio.http.Request

case object GithubSourceHandler extends SourceHandler {
  private val eventMap: Map[String, GithubSourceEventHandler] = Map(
    "push" -> events.PushEvent,
    "ping" -> events.PingEvent
  )

  override def determineEvent(req: Request): Result[GithubSourceEventHandler] =
    ZIO.attempt(eventMap(req.headers.get("X-GitHub-Event").get.toLowerCase)).orDie // TODO: Handle this properly
}
