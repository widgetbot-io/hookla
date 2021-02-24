package venix.hookla.util

import io.circe._
import io.finch._

object ExceptionEncoder {
  def encodeErrorList(es: List[Exception]): Json = {
    val messages = es.map(x => Json.fromString(x.getMessage))
    Json.obj("errors" -> Json.arr(messages: _*))
  }

  implicit val encodeException: Encoder[Throwable] = Encoder.instance({
    case e: io.finch.Errors => encodeErrorList(e.errors.toList)
    case e: io.finch.Error =>
      e.getCause match {
        case e: io.circe.Errors => encodeErrorList(e.errors.toList)
        case _                  => Json.obj("message" -> Json.fromString(e.getMessage))
      }
    case _: NotImplementedError => // Normally you shouldn't catch this as its a java "Error", not Exception, but this is fine because it's caught within finch.
      Json.obj("message" -> Json.fromString("This event is not currently implemented."))
    case e: Exception =>
      Json.obj("message" -> Json.fromString(e.getMessage))
  })
}
