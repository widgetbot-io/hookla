package venix.hookla.resolvers

import caliban.RootResolver
import venix.hookla.{Mutations, Queries}
import zio.{UIO, ZIO, ZLayer}

class SchemaResolver(
    private val sourceResolver: SourceResolver,
    private val sinkResolver: SinkResolver
) {
  def rootResolver: UIO[RootResolver[Queries, Mutations, Unit]] =
    ZIO.succeed {
      RootResolver(
        Queries(
          sinks = sinkResolver.getAll,
          sources = sourceResolver.getAll
        ),
        Mutations(
          identity
        )
      )
    }
}

object SchemaResolver {
  private type In = SourceResolver with SinkResolver
  private def create(sourceResolver: SourceResolver, sinkResolver: SinkResolver) = new SchemaResolver(sourceResolver, sinkResolver)

  val live: ZLayer[In, Throwable, SchemaResolver] = ZLayer.fromFunction(create _)
}
