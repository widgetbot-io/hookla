package venix.hookla.models

import java.time.OffsetDateTime
import java.util.UUID

case class HookSink(
    hookId: UUID,
    sinkId: String,
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime
)
