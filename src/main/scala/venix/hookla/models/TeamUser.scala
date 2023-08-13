package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class TeamUser(
    teamId: UUID,
    userId: UUID,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
