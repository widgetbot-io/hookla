package venix.hookla

import caliban.schema.Annotations._
import venix.hookla.entities._

import java.util.UUID

object Args {
  case class CreateTeamArgs(name: String)
  case class UpdateTeamArgs(id: UUID, name: String)
  case class DeleteTeamArgs(id: UUID)
  case class AddTeamMemberArgs(teamId: UUID, userId: UUID)
  case class UpdateTeamMemberArgs(teamId: UUID, userId: UUID, admin: Boolean)
  case class RemoveTeamMemberArgs(teamId: UUID, userId: UUID)
}

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
    teams: Task[List[Team]]
)

final case class Mutations(
    @GQLDescription("Creates a new team")
    createTeam: Args.CreateTeamArgs => Task[Team],
    @GQLDescription("Updates an existing team")
    updateTeam: Args.UpdateTeamArgs => Task[Team],
    @GQLDescription("Deletes an existing team")
    deleteTeam: Args.DeleteTeamArgs => Task[Unit],
    @GQLDescription("Adds a user to a team")
    addTeamMember: Args.AddTeamMemberArgs => Task[Unit],
    @GQLDescription("Updates a user's admin status in a team")
    updateTeamMember: Args.UpdateTeamMemberArgs => Task[Unit],
    @GQLDescription("Removes a user from a team")
    removeTeamMember: Args.RemoveTeamMemberArgs => Task[Unit]
)
