package venix.hookla.models

import java.util.{Date, UUID}

case class HookSettings(
    id: UUID,
    name: String,
    default: Boolean,
    hookId: UUID,
    createdAt: Date,
    updatedAt: Date
)
