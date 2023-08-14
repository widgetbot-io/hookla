package venix.hookla.models

import venix.hookla.types.{HookId, TeamId}

import java.util.Date

case class Hook(
    id: HookId,
    teamId: TeamId,
    sourceId: String,
    createdAt: Date,
    updatedAt: Date
)
