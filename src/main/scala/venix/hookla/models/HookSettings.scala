package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class HookSettings(
    id: UUID,
    name: String,
    default: Boolean,
    hookId: UUID,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
