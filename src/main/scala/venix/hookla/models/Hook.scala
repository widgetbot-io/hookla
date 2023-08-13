package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class Hook(
    id: UUID,
    teamId: UUID,
    sourceId: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
