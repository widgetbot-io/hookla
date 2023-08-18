package venix.hookla.sources.github

import venix.hookla.Result
import venix.hookla.sources.{SourceEventHandler, SourceHandler}
import zio.ZIO
import zio.http.Request

case object GithubSourceHandler extends SourceHandler {
  private val eventMap: Map[String, SourceEventHandler] = Map(
    "push" -> events.PushEvent
  )

  override def determineEvent(req: Request): Result[SourceEventHandler] =
    ZIO.attempt(eventMap(req.headers.get("X-GitHub-Event").get.toLowerCase)).orDie // TODO: Handle this properly
}
