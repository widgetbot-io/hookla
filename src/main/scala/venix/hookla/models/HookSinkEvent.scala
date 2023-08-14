package venix.hookla.models

import java.util.{Date, UUID}

case class HookSinkEvent(
    hookSinkId: UUID,
    hookSettingsId: Option[UUID],
    eventId: String,
    createdAt: Date,
    updatedAt: Date
)
