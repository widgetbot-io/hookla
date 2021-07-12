package venix.hookla.handlers

import venix.hookla.types.{BasePayload, EventData}
import venix.hookla.util.EventHandlerUtils

trait BaseEvent[T <: BasePayload] extends EventHandlerUtils {
  def handleEvent(payload: T, data: EventData): Unit
}

