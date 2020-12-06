package venix.hookla.controllers

import cats.effect._
import io.circe.generic.auto._
import io.finch.Endpoint
import io.circe.Json
import io.finch.circe._
import io.finch._
import javax.inject.Inject

import venix.hookla.services.ProviderService

class WebhookController @Inject()(
  providerService: ProviderService
) extends BaseController {
  def endpoints: Endpoint[IO, String] = process

  def pathProviderId: Endpoint[IO, String] = path[String]

  def process: Endpoint[IO, String] = post("process" :: pathProviderId :: jsonBody[Json] :: headersAll) { (providerId: String, body: Json, headers: Map[String, String]) =>
    val provider = providerService.getById(providerId)

    println("hit")

    provider match {
      case None => Ok("success")
      case Some(provider) => {
        Ok("success but actually a success")
      }
    }
  }
}
