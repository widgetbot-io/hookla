package venix.hookla.models

import venix.hookla.types.{HookId, HookSettingsId}

import java.util.Date

case class HookSettings(
    id: HookSettingsId,
    name: String,
    default: Boolean,
    hookId: HookId,
    createdAt: Date,
    updatedAt: Date
)
