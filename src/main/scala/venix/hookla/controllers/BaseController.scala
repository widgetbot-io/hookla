package venix.hookla.controllers

import cats.effect.IO
import io.finch.Endpoint

trait BaseController extends Endpoint.Module[IO] {}
