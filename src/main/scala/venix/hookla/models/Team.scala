package venix.hookla.models

import java.util.{Date, UUID}

case class Team(
    id: UUID,
    name: String,
    createdAt: Date,
    updatedAt: Date
)
