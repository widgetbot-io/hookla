package venix.hookla.models

import java.util.{Date, UUID}

case class Hook(
    id: UUID,
    teamId: UUID,
    sourceId: String,
    createdAt: Date,
    updatedAt: Date
)
