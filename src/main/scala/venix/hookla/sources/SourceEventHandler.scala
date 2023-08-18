package venix.hookla.sources

import venix.hookla.Task
import venix.hookla.models.Hook
import zio.http.Request

trait SourceEventHandler {
  def handle(request: Request, hook: Hook): Task[Unit]
}
