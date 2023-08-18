package venix.hookla.sources.github.events

import venix.hookla.Task
import venix.hookla.models.Hook
import venix.hookla.sources.SourceEventHandler
import zio.ZIO
import zio.http.Request

private[github] case object PushEvent extends SourceEventHandler {
  override def handle(request: Request, hook: Hook): Task[Unit] = {
    println("hello there")

    ZIO.unit
  }
}
