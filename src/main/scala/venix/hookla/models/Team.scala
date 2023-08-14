package venix.hookla.models

import venix.hookla.types.TeamId

import java.util.Date

case class Team(
    id: TeamId,
    name: String,
    createdAt: Date,
    updatedAt: Date
)
