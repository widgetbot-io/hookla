package venix.hookla.models

import venix.hookla.types.{HookId, HookSinkId}

import java.util.Date

case class HookSink(
    id: HookSinkId,
    hookId: HookId,
    sinkId: String,
    createdAt: Date,
    updatedAt: Date
)
