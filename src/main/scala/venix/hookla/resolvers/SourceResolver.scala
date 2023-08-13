package venix.hookla.resolvers

import venix.hookla.entities.Source
import zio.{UIO, ZIO, ZLayer}

class SourceResolver {
  def getAll: UIO[List[Source]] = ZIO.succeed(Nil)
}

object SourceResolver {
  private def create() = new SourceResolver()

  val live: ZLayer[Any, Throwable, SourceResolver] = ZLayer.fromFunction(create _)
}
