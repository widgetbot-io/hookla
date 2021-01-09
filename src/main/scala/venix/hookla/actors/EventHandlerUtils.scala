package venix.hookla.actors

import venix.hookla.models.EmbedOptions

trait EventHandlerUtils {
  protected def formatCommit(message: String, length: Int, embedOptions: Option[EmbedOptions]): String = {
    val defaultChar = "!" :: "$" :: Nil
    val defaultReverts = defaultChar.map(c => s"Revert ${c}")
    val defaultMsg = "This commit message has been marked as private."

    embedOptions.fold {
      val privateDenotations = defaultChar ::: defaultReverts ::: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      s"${if (length > 1) "- " else ""}${if (isPrivate) defaultMsg else message}"
    } { embedOptions =>
      val privateChar: List[String] = embedOptions.privateCharacter.map(List(_)).getOrElse(defaultChar)
      val privateReverts = privateChar.map(c => s"Revert ${c}")
      val privateDenotations = privateChar ::: privateReverts ::: defaultChar ::: defaultReverts ::: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      s"${if (length > 1) "- " else ""}${if (isPrivate) embedOptions.privateMessage.getOrElse(defaultMsg) else message}"
    }
   }
}
