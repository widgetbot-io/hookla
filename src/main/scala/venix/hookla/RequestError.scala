package venix.hookla

import caliban.CalibanError.ExecutionError
import caliban.schema.Schema
import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.resolvers.IUserResolver
import zio.{IO, ZIO}

sealed trait RequestError

object RequestError {
  sealed trait HTTPError extends RequestError

  case class DecodingError(e: io.circe.Error)                 extends RequestError
  case class DatabaseError(message: String, cause: Throwable) extends RequestError
  case class InvalidRequest(message: String)                  extends RequestError
  case class InvalidRequestPayload(message: String)           extends RequestError
  case object UnknownError                                    extends RequestError

  case class RequestTimeout(message: String)       extends HTTPError
  case class InternalServerError(message: String)  extends HTTPError
  case class ServiceUnavailable(message: String)   extends HTTPError
  case class GatewayTimeout(message: String)       extends HTTPError
  case class TooManyRequests(message: String)      extends HTTPError
  case class BadRequest(message: String)           extends HTTPError
  case class Unauthorized(message: String)         extends HTTPError
  case class DeserializationError(message: String) extends HTTPError
  case class GenericHttpError(message: String)     extends HTTPError

  type Env             = HooklaConfig with ZioJAsyncConnection with IUserResolver
  type Result[T]       = IO[RequestError, T]
  type Task[T]         = ZIO[Env, RequestError, T]
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
