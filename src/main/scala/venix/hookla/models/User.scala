package venix.hookla.models

import venix.hookla.types.UserId

import java.util.{Date, UUID}

case class User(
    id: UserId,
    discordId: String,
    createdAt: Date
)
