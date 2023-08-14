package venix.hookla.resolvers

import caliban.RootResolver
import venix.hookla.{Mutations, Queries}
import zio.{UIO, ZIO, ZLayer}

trait ISchemaResolver {
  def rootResolver: UIO[RootResolver[Queries, Mutations, Unit]]
}

class SchemaResolver(
    private val userResolver: IUserResolver,
    private val sourceResolver: ISourceResolver,
    private val sinkResolver: ISinkResolver
) extends ISchemaResolver {
  def rootResolver: UIO[RootResolver[Queries, Mutations, Unit]] =
    ZIO.succeed {
      RootResolver(
        Queries(
          sinks = sinkResolver.getAll,
          sources = sourceResolver.getAll,
          me = userResolver.me,
          teams = ZIO.succeed(Nil)
        ),
        Mutations(
          identity
        )
      )
    }
}

object SchemaResolver {
  private type In = ISourceResolver with ISinkResolver with IUserResolver
  private def create(sourceResolver: ISourceResolver, sinkResolver: ISinkResolver, userResolver: IUserResolver) = new SchemaResolver(userResolver, sourceResolver, sinkResolver)

  val live: ZLayer[In, Throwable, ISchemaResolver] = ZLayer.fromFunction(create _)
}
