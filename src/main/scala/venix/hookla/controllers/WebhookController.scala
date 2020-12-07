package venix.hookla.controllers

import akka.NotUsed
import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import cats.Inject
import cats.effect._
import io.circe.generic.auto._
import io.finch.Endpoint
import io.circe.Json
import io.finch.circe._
import io.finch._
import venix.hookla.actors.{EventHandler, EventHandlerCommand, GitlabEvent, GitlabEventHandler}

@Singleton
class WebhookController @Inject()(
    actor: ActorRef[GitlabEvent]
) extends BaseController {
  def endpoints = process

  def main(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val gitlabEventHandler = ctx.spawn(GitlabEventHandler(), "gitlabEventHandler")
      val eventHandler = ctx.spawn(EventHandler(gitlabEventHandler), "eventHandler")
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  val as = ActorSystem(main(), "Hookla")
  val eventHandler = as.systemActorOf[EventHandlerCommand]

  def process: Endpoint[IO, String] = post(apiBase :: "process" :: jsonBody[Json] :: headersAll) { (body: Json, headers: Map[String, String]) =>
    println(body)

    println(headers)

    Ok("meme")
  }
}
