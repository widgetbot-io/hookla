package venix.hookla.sources

import zio.{URIO, ZIO}
import zio.http.Request

trait SourceHandler {
  /*
   * This method is called when a webhook is received.
   * It should determine the event type and then call the appropriate method.
   * The event type is determined by the source, and the source is determined by the request.
   * The request is passed in so that the handler can determine the event type.
   * i.e. push, issue, deployment, etc...
   */
  def determineEvent(req: Request): SourceEventHandler
}

object SourceHandler {
  def getHandlerById(id: String): URIO[Any, SourceHandler] =
    id match {
      case "github" => ???
    }
}
