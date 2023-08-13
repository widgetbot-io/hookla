package venix.hookla

import caliban.schema.Annotations.GQLDescription
import venix.hookla.RequestError._
import venix.hookla.entities.{Sink, Source}

final case class Queries(
    @GQLDescription("Returns all available sinks")
    sinks: Result[List[Sink]],
    @GQLDescription("Returns all available sources")
    sources: Result[List[Source]]
)

final case class Mutations(
    test: String => String
)
