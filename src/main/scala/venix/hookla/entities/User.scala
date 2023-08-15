package venix.hookla.entities

import caliban.schema.Annotations.{GQLDescription, GQLExcluded}

import java.util.UUID

case class User(
    id: UUID,
    @GQLDescription("Is the user an admin of the team, only available on Team.members")
    teamAdmin: Boolean = false,
    @GQLExcluded
    discordId: String
//    discord: Task[Option[DiscordUser]],
//    teams: Task[List[Team]]
)
