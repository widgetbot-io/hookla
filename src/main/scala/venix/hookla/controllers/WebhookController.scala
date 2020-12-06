package venix.hookla.controllers

import cats.effect._
import io.circe.generic.auto._
import io.finch.Endpoint
import io.circe.Json
import io.finch.circe._
import io.finch._

class WebhookController extends BaseController {
  def endpoints = process

  def process: Endpoint[IO, String] = post(apiBase :: "process" :: jsonBody[Json] :: headersAll) { (body: Json, headers: Map[String, String]) =>
    println(body)

    println(headers)

    Ok("meme")
  }
}
