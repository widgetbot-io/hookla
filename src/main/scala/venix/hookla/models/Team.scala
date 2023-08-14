package venix.hookla.models

import venix.hookla.entities
import venix.hookla.types.TeamId
import zio.ZIO

import java.util.Date

case class Team(
    id: TeamId,
    name: String,
    createdAt: Date,
    updatedAt: Date
) {
  def toEntity: entities.Team =
    entities.Team(
      id = id.unwrap,
      name = name,
      members = ZIO.succeed(Nil)
    )
}
