package venix.hookla

import caliban.CalibanError.ExecutionError
import caliban.schema.Schema
import zio.IO

sealed trait RequestError
object RequestError {
  case class DatabaseError(message: String, cause: Throwable) extends RequestError
  case class InvalidRequest(message: String)                  extends RequestError
  case class InvalidRequestPayload(message: String)           extends RequestError

  type Result[T]       = IO[RequestError, T]
  type CustomSchema[T] = Schema[Any, T]

  implicit def customEffectSchema[A: CustomSchema]: CustomSchema[Result[A]] =
    Schema.customErrorEffectSchema {
      case DatabaseError(message, cause)  => ExecutionError(message)
      case InvalidRequest(message)        => ExecutionError(message)
      case InvalidRequestPayload(message) => ExecutionError(message)
      case _                              => ExecutionError("Something went wrong")
    }
}
