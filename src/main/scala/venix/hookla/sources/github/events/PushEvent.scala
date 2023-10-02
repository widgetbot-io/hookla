package venix.hookla.sources.github.events

import io.circe.Json
import venix.hookla.Task
import venix.hookla.models.Hook
import venix.hookla.sources.{GithubSourceEventHandler, SourceEventHandler}
import zio.ZIO
import zio.http.Request

private[github] case object PushEvent extends GithubSourceEventHandler {
  def handle(body: Json, headers: Map[String, String], hook: Hook): Task[Unit] = {
    println("hello there")

    ZIO.unit
  }
}
