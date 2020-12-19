package venix.hookla.models

import java.util.UUID

case class EmbedOptions(
    id: UUID,
    userId: UUID,

    // Customizable format.
    descriptionFormat: String
)
