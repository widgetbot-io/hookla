package venix.hookla.actors

import venix.hookla.models.EmbedOptions

trait EventHandlerUtils {
  protected def formatCommit(message: String, length: Int, embedOptions: Option[EmbedOptions]): String = {
    val defaultChar = "!"
    val defaultMsg = "This commit message has been marked as private."

    embedOptions.fold {
      val privateDenotations = defaultChar :: s"Revert $defaultChar" :: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      s"${if (length > 1) "- " else ""}${if (isPrivate) defaultMsg else message}"
    } { embedOptions =>
      val privateChar = embedOptions.privateCharacter.getOrElse(defaultChar)
      val privateDenotations = privateChar :: s"Revert $privateChar" :: defaultChar :: s"Revert $defaultChar" :: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      s"${if (length > 1) "- " else ""}${if (isPrivate) embedOptions.privateMessage.getOrElse(defaultMsg) else message}"
    }
   }
}
