package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class User(
    id: UUID,
    discordId: String,
    createdAt: OffsetDateTime
)
