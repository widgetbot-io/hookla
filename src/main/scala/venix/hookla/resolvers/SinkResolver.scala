package venix.hookla.resolvers

import venix.hookla.entities.Sink
import zio.{UIO, ZIO, ZLayer}

class SinkResolver {
  def getAll: UIO[List[Sink]] = ZIO.succeed(Nil)
}

object SinkResolver {
  private def create() = new SinkResolver()

  val live: ZLayer[Any, Throwable, SinkResolver] = ZLayer.fromFunction(create _)
}
