package venix.hookla.controllers

import cats.effect.IO
import io.finch.Endpoint
import org.slf4j.LoggerFactory
import shapeless.HNil

trait BaseController extends Endpoint.Module[IO] {
  protected lazy val logger = LoggerFactory.getLogger(getClass)
  def apiBase: Endpoint[IO, HNil] = "api"

  // Finch Helpers
  protected val headersAll: Endpoint[IO, Map[String, String]] = root.map(_.headerMap.toMap)
}
