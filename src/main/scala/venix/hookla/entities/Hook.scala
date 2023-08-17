package venix.hookla.entities

import caliban.schema.Annotations.GQLExcluded

import java.util.UUID

case class Hook(
    id: UUID,
    @GQLExcluded teamId: UUID,
    @GQLExcluded sourceId: String
)
