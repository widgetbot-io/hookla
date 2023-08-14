package venix.hookla.entities

import venix.hookla.RequestError.Result

import java.util.UUID

case class Team(
    id: UUID,
    name: String,
    members: Result[List[User]]
)
