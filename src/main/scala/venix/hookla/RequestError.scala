package venix.hookla

import caliban.CalibanError.ExecutionError
import caliban.schema.Schema
import io.getquill.context.zio.ZioJAsyncConnection
import venix.hookla.resolvers.IUserResolver
import zio.{IO, ZIO}

sealed trait RequestError extends Throwable

object RequestError {
  sealed trait HTTPError extends RequestError

  case class Unauthenticated(message: String) extends RequestError
  case class Forbidden(message: String)       extends RequestError

  case class DecodingError(e: io.circe.Error)       extends RequestError
  case class DatabaseError(e: Throwable)            extends RequestError
  case class RedisError(e: zio.redis.RedisError)    extends RequestError
  case class InvalidRequest(message: String)        extends RequestError
  case class InvalidRequestPayload(message: String) extends RequestError

  case class UnhandledError(error: Throwable) extends RequestError
  case object UnknownError                    extends RequestError

  case class RequestTimeout(message: String)       extends HTTPError
  case class InternalServerError(message: String)  extends HTTPError
  case class ServiceUnavailable(message: String)   extends HTTPError
  case class GatewayTimeout(message: String)       extends HTTPError
  case class TooManyRequests(message: String)      extends HTTPError
  case class BadRequest(message: String)           extends HTTPError
  case class Unauthorized(message: String)         extends HTTPError
  case class DeserializationError(message: String) extends HTTPError
  case class GenericHttpError(message: String)     extends HTTPError
}
