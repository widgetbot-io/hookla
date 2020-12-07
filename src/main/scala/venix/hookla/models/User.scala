package venix.hookla.models

import java.util.{Date, UUID}

case class User (
    id: UUID,
    discordId: String,

    createdAt: Date
)
