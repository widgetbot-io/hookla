package venix.hookla

import caliban.CalibanError.ExecutionError
import caliban.schema.Schema
import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.resolvers.IUserResolver
import zio.IO

sealed trait RequestError
object RequestError {
  case class DatabaseError(message: String, cause: Throwable) extends RequestError
  case class InvalidRequest(message: String)                  extends RequestError
  case class InvalidRequestPayload(message: String)           extends RequestError
  case object UnknownError                                    extends RequestError

  type Env             = HooklaConfig with ZioJAsyncConnection with IUserResolver
  type Result[T]       = IO[RequestError, T]
  type CustomSchema[T] = Schema[Any, T]

  implicit def customEffectSchema[A: CustomSchema]: CustomSchema[Result[A]] =
    Schema.customErrorEffectSchema {
      case DatabaseError(message, cause)  => ExecutionError(message)
      case InvalidRequest(message)        => ExecutionError(message)
      case InvalidRequestPayload(message) => ExecutionError(message)
      case UnknownError                   => ExecutionError("Something went wrong, please try again later.")
      case _                              => ExecutionError("Something went wrong, please don't try again later.")
    }
}
