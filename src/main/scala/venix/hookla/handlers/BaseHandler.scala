package venix.hookla.handlers

import venix.hookla.types.{BasePayload, EventData, Provider}

trait BaseHandler[T <: BasePayload] {
  def handle(payload: T, data: EventData): Unit
}
