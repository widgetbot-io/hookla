package venix.hookla.resolvers

import venix.hookla.entities.Sink
import zio.{UIO, ZIO, ZLayer}

trait ISinkResolver {
  def getAll: UIO[List[Sink]]
}

class SinkResolver extends ISinkResolver {
  def getAll: UIO[List[Sink]] = ZIO.succeed(Nil)
}

object SinkResolver {
  private def create() = new SinkResolver()

  val live: ZLayer[Any, Throwable, ISinkResolver] = ZLayer.fromFunction(create _)
}
