package venix.hookla.actors

trait EventHandlerUtils {
  protected def formatCommit(message: String, length: Int): String =
    s"${if (length > 1) "- " else ""}${message.replace("\n", "")}"
}
