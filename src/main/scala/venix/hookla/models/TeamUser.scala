package venix.hookla.models

import java.util.{Date, UUID}

case class TeamUser(
    teamId: UUID,
    userId: UUID,
    createdAt: Date,
    updatedAt: Date
)
