package venix.hookla.resolvers

import venix.hookla.entities.Source
import zio.{UIO, ZIO, ZLayer}

trait SourceResolver {
  def getAll: UIO[List[Source]]
}

private class SourceResolverImpl extends SourceResolver {
  def getAll: UIO[List[Source]] = ZIO.succeed(Nil)
}

object SourceResolver {
  private def create() = new SourceResolverImpl()

  val live: ZLayer[Any, Throwable, SourceResolver] = ZLayer.fromFunction(create _)
}
