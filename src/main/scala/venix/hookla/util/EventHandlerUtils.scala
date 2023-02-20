package venix.hookla.util

import venix.hookla.models.EmbedOptions

trait EventHandlerUtils {
  private val defaultChars: List[String] = "!" :: "$" :: Nil

  protected def isPrivateBranch(branchName: String): Boolean = defaultChars.exists(branchName.startsWith)
  protected def getBranchFromRef(ref: String): String        = ref.split('/').drop(2).mkString("/")

  protected def formatCommit(message: String, length: Int, url: String, embedOptions: Option[EmbedOptions]): String = {
    val defaultReverts = defaultChars.map(c => s"Revert $c")
    val defaultMsg     = "This commit message has been marked as private."

    embedOptions.fold {
      val privateDenotations = defaultChars ::: defaultReverts ::: Nil
      val isPrivate          = privateDenotations.exists(message.startsWith)

      s"${if (length > 1) "- " else ""}${if (isPrivate) defaultMsg else message}"
    } { embedOptions =>
      val privateChar: List[String] = embedOptions.privateCharacter.map(List(_)).getOrElse(defaultChars)
      val privateReverts            = privateChar.map(c => s"Revert $c")
      val privateDenotations        = privateChar ::: privateReverts ::: defaultChars ::: defaultReverts ::: Nil
      val isPrivate                 = privateDenotations.exists(message.startsWith)
      val clickableMsg              = if (embedOptions.areCommitsClickable) s"[$message]($url)" else message

      s"${if (length > 1) "- " else ""}${if (isPrivate) embedOptions.privateMessage.getOrElse(defaultMsg) else clickableMsg}"
    }
  }
}
