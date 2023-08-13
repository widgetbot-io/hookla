package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class Team(
    id: UUID,
    name: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
