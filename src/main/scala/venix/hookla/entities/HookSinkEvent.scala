package venix.hookla.entities

import venix.hookla.types.{HookSettingsId, HookSinkId}

import java.util.Date

case class HookSinkEvent(
    hookSinkId: HookSinkId,
    hookSettingsId: HookSettingsId,
    eventId: String,
    createdAt: Date,
    updatedAt: Date
)
