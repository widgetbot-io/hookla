package venix.hookla.entities

import caliban.schema.Annotations.{GQLDefault, GQLDescription}
import venix.hookla.RequestError.Result

import java.util.UUID

case class User(
    id: UUID,
    discord: Result[Option[DiscordUser]],
    teams: Result[List[Team]],
    @GQLDescription("Is the user an admin of the team, only available on Team.members")
    teamAdmin: Boolean = false
)
