package venix.hookla.handlers

import venix.hookla.types.{BasePayload, EventData}
import org.log4s._

trait BaseHandler[T <: BasePayload] {
  protected lazy val logger: Logger = getLogger

  def handle(payload: T, data: EventData): Unit
}
