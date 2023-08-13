package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class HookSinkEvent(
    hookSinkId: UUID,
    hookSettingsId: Option[UUID],
    eventId: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
