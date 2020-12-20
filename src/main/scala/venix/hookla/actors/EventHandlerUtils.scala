package venix.hookla.actors

import venix.hookla.models.EmbedOptions

trait EventHandlerUtils {
  protected def formatCommit(
    message: String,
    length: Int,
    embedOptions: Option[EmbedOptions]
  ): String = {
    val defaultChar = "!"
    val defaultPrivateMsg = "This commit message has been marked as private."

    val msg = embedOptions.fold {
      val privateDenotations = defaultChar :: s"Revert $defaultChar" :: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      if (isPrivate) defaultPrivateMsg else message
    } { embedOptions =>
      val privateChar = embedOptions.privateCharacter.getOrElse(defaultChar)
      val privateDenotations = privateChar :: s"Revert $privateChar" :: defaultChar :: s"Revert $defaultChar" :: Nil
      val isPrivate = privateDenotations.exists(message.startsWith)

      if (isPrivate) embedOptions.privateMessage.getOrElse(defaultPrivateMsg) else message
    }

    s"${if (length > 1) "- " else ""}$msg"
   }

  private def formatDescription(
    format: String,
    data: Map[String, Any]
  ): String =
    data.foldLeft(format)((str, r) => str.replace(r._1, r._2.toString))
}
