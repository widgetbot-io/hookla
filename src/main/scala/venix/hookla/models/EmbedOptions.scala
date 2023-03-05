package venix.hookla.models

import java.util.UUID

case class EmbedOptions(
    id: UUID,
    userId: UUID,
    // Makes them a hyperlink.
    areCommitsClickable: Boolean,
    // Shows private commits.
    showPrivateCommits: Boolean,
    // Prepends the private commit message with a prefix.
    privateCommitPrefix: Option[String],
    // Customizable format.
    descriptionFormat: Option[String],
    // Private commit customization
    privateMessage: Option[String],
    privateCharacter: Option[String]
)
