package venix.hookla.actors

trait EventHandlerUtils {
  protected def formatCommit(message: String, length: Int): String = {
    val privateDenotations = List("!", "$", "Revert !", "Revert $")
    val isPrivate = privateDenotations.exists(message.startsWith)

    s"${if (length > 1) "- " else ""}${if (isPrivate) "This commit message has been marked as private." else message}"
  }
}
