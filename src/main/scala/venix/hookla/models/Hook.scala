package venix.hookla.models

import venix.hookla.types.{HookId, TeamId}

import java.util.Date

case class Hook(
    id: HookId,
    teamId: TeamId,
    sourceId: String,
    createdAt: Date,
    updatedAt: Date
) {
  def toEntity: venix.hookla.entities.Hook =
    venix.hookla.entities.Hook(
      id = id.unwrap,
      teamId = teamId.unwrap,
      sourceId = sourceId
    )
}
