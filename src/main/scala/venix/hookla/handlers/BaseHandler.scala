package venix.hookla.handlers

import venix.hookla.types.{BasePayload, HandlerData, Provider}

trait BaseHandler[T <: BasePayload] {
  def handle(payload: T, data: HandlerData): Unit
}
