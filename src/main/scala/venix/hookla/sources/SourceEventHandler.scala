package venix.hookla.sources

import io.circe.Json
import venix.hookla.Task
import venix.hookla.models.Hook

/**
  * This trait is used to handle the body of a webhook, after the event type has been determined.
  * The reason there is a type argument is because different sources might use different formats i.e. JSON, XML, etc...
  *
  * @tparam T The type of the body of the webhook (i.e. Json, String, case class, etc...)
  */
sealed trait SourceEventHandler[T <: Serializable] {
  def handle(body: T, headers: Map[String, String], hook: Hook): Task[Unit]
}

trait GithubSourceEventHandler extends SourceEventHandler[Json] {
  def handle(body: Json, headers: Map[String, String], hook: Hook): Task[Unit]
}
