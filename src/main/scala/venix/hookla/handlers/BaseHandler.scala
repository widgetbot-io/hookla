package venix.hookla.handlers

import venix.hookla.types.{BasePayload, EventData}

trait BaseHandler[T <: BasePayload] {
  def handle(payload: T, data: EventData): Unit
}
