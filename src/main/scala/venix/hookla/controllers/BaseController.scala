package venix.hookla.controllers

import cats.effect.IO
import io.finch.Endpoint
import org.log4s._

trait BaseController extends Endpoint.Module[IO] {
  protected lazy val logger: Logger = getLogger

  // Finch Helpers
  protected val headersAll: Endpoint[IO, Map[String, String]] = root.map(_.headerMap.toMap)
}
