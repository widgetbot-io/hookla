package venix.hookla.models

import java.util.{Date, UUID}

case class HookSink(
    hookId: UUID,
    sinkId: String,
    createdAt: Date,
    updatedAt: Date
)
