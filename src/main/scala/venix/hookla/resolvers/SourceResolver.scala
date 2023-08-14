package venix.hookla.resolvers

import venix.hookla.entities.Source
import zio.{UIO, ZIO, ZLayer}

trait ISourceResolver {
  def getAll: UIO[List[Source]]
}

class SourceResolver extends ISourceResolver {
  def getAll: UIO[List[Source]] = ZIO.succeed(Nil)
}

object SourceResolver {
  private def create() = new SourceResolver()

  val live: ZLayer[Any, Throwable, ISourceResolver] = ZLayer.fromFunction(create _)
}
