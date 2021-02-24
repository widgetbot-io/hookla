package venix.hookla.handlers

import venix.hookla.types.{BasePayload, HandlerData}
import venix.hookla.util.EventHandlerUtils

trait BaseHandler[T <: BasePayload] extends EventHandlerUtils {
  def handleEvent(payload: T, data: HandlerData)
}
