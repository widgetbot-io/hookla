package venix.hookla.sources

import venix.hookla.Result
import venix.hookla.sources.github.GithubSourceHandler
import zio.{UIO, URIO, ZIO}
import zio.http.Request

private[sources] trait SourceHandler {
  /*
   * This method is called when a webhook is received.
   * It should determine the event type and then call the appropriate method.
   * The event type is determined by the source, and the source is determined by the request.
   * The request is passed in so that the handler can determine the event type.
   * i.e. push, issue, deployment, etc...
   */
  def determineEvent(req: Request): Result[SourceEventHandler[_ <: Serializable]]
}

object SourceHandler {
  def getHandlerById(id: String): UIO[SourceHandler] =
    id match {
      case "github" => ZIO.succeed(GithubSourceHandler)
    }
}
