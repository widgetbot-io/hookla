package venix.hookla.sources.github.events

import io.circe.Json
import venix.hookla.Task
import venix.hookla.models.Hook
import venix.hookla.sources.GithubSourceEventHandler
import zio.ZIO

private[github] case object PingEvent extends GithubSourceEventHandler {
  def handle(body: Json, headers: Map[String, String], hook: Hook): Task[Unit] = {
    println("hello there")

    ZIO.unit
  }
}
