package venix.hookla.models

import venix.hookla.entities
import venix.hookla.resolvers.UserResolver
import venix.hookla.types.UserId

import java.util.{Date, UUID}

case class User(
    id: UserId,
    discordId: String,
    createdAt: Date
) {
  def toEntity: entities.User =
    venix.hookla.entities.User(
      id = id.unwrap,
      discordId = discordId
    )
}
