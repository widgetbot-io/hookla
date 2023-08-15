package venix.hookla

import caliban.schema.Annotations._
import venix.hookla.RequestError._
import venix.hookla.entities._

final case class Queries(
    // Public queries
    @GQLDescription("Returns all available sinks")
    sinks: Result[List[Sink]],
    @GQLDescription("Returns all available sources")
    sources: Result[List[Source]],
    // Authenticated queries
    @GQLDescription("Returns the current user")
    me: Task[User],
    @GQLDescription("Returns all teams the current user is a member of")
    teams: Result[List[Team]]
)

final case class Mutations(
    test: String => String
)
