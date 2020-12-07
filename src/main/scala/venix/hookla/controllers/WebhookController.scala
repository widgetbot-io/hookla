package venix.hookla.controllers

import akka.NotUsed
import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import cats.effect._
import io.circe.generic.auto._
import com.google.inject.{Inject, Singleton}
import io.finch.Endpoint
import io.circe.Json
import io.finch.circe._
import io.finch._
import venix.hookla.actors.{EventHandler, EventHandlerCommand, Github, Gitlab, GitlabEventHandler}

@Singleton
class WebhookController @Inject()(
    actor: ActorRef[EventHandlerCommand]
) extends BaseController {
  def endpoints = process

  def process: Endpoint[IO, String] = post(apiBase :: "process" :: jsonBody[Json] :: headersAll) { (body: Json, headers: Map[String, String]) =>
    println(body)

    println(headers)

    actor ! Gitlab.PushEvent("test")
    actor ! Github.PushEvent("test")

    Ok("meme")
  }
}
