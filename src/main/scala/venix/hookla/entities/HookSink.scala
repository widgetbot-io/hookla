package venix.hookla.entities

import caliban.schema.Annotations.GQLExcluded

import java.util.{Date, UUID}

case class HookSink(
    id: UUID,
    @GQLExcluded hookId: UUID,
    @GQLExcluded sinkId: String,
    createdAt: Date,
    updatedAt: Date
)
