package venix.hookla.models

import venix.hookla.types.{HookSettingsId, HookSinkId}

import java.util.Date

case class HookSinkEvent(
    hookSinkId: HookSinkId,
    hookSettingsId: Option[HookSettingsId],
    eventId: String,
    createdAt: Date,
    updatedAt: Date
)
