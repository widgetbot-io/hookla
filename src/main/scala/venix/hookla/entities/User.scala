package venix.hookla.entities

import venix.hookla.RequestError.Result

import java.util.UUID

case class User(
    id: UUID
//    discord: Result[Option[DiscordUser]],
//    teams: Result[List[Team]]
)
