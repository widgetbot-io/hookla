package venix.hookla.models

import venix.hookla.types.{TeamId, UserId}

import java.util.Date

case class TeamUser(
    teamId: TeamId,
    userId: UserId,
    admin: Boolean,
    createdAt: Date,
    updatedAt: Date
)
