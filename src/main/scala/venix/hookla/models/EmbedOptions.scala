package venix.hookla.models

import java.util.UUID

case class EmbedOptions(
    id: UUID,
    userId: UUID,

    // Customizable format.
    descriptionFormat: Option[String],

    // Private commit customization
    privateMessage: Option[String],
    privateCharacter: Option[String]
)
