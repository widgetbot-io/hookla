package venix.hookla.services.core

import io.circe.Codec
import io.circe.syntax._
import pdi.jwt.exceptions.JwtValidationException
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import venix.hookla.RequestError.{DecodingError, RedisError, Unauthenticated, UnhandledError}
import venix.hookla.{HooklaConfig, Result, Task}
import venix.hookla.models.User
import venix.hookla.services.db.UserService
import venix.hookla.types.UserId
import zio._
import zio.redis.Redis

import java.time.Instant
import java.util.UUID
import scala.language.postfixOps

trait AuthService {
  def createToken(user: User): Result[String]
  def decodeToken(token: String): Result[User]
}

private class AuthServiceImpl(
    private val config: HooklaConfig,
    private val redis: Redis,
    private val userService: UserService
) extends AuthService {
  private case class AuthToken(id: UUID)
  implicit private val authTokenCodec: Codec[AuthToken] = io.circe.generic.semiauto.deriveCodec

  private def redisKey(userId: UserId): String = s"authkeys:${userId.unwrap.toString}"

  override def createToken(user: User): Result[String] = {
    val claim = createClaim(user.id)
    store(redisKey(user.id), claim, 7 days).mapBoth(RedisError, _ => claim)
  }

  override def decodeToken(token: String): Result[User] =
    ZIO
      .fromTry(JwtCirce.decodeJson(token, config.auth.jwtSecret, Seq(JwtAlgorithm.HS512)))
      .map(_.as[AuthToken])
      .absolve
      // TODO: Re-enable token expiry when not testing
//      .flatMap(authToken => isTokenValid(redisKey(UserId(authToken.id))).map(v => (authToken, v)))
//      .filterOrFail(_._2)(Unauthenticated("Expired token"))
//      .map { case (authToken, _) => authToken }
      .flatMap(authToken => userService.getById(UserId(authToken.id)).map(_.get)) // TODO: Remove this .get
      .mapError {
        case e: io.circe.Error         => DecodingError(e)
        case e: JwtValidationException => Unauthenticated("Invalid token")
        case e =>
          e.printStackTrace()
          UnhandledError(e)
      }

  private def store(key: String, value: String, expiry: Duration) = redis.set(key, value, Some(expiry)).unit
  private def isTokenValid(key: String)                           = redis.get(key).returning[String].map(_.isDefined)

  private def createClaim(userId: UserId, expiry: Int = 604800): String = {
    val claim = JwtClaim(
      content = AuthToken(userId.unwrap).asJson.noSpaces,
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some("Hookla"),
      expiration = Some(Instant.now.plusSeconds(expiry).getEpochSecond) // A week
    )

    JwtCirce.encode(claim, config.auth.jwtSecret, JwtAlgorithm.HS512)
  }
}

object AuthService {
  private type In = HooklaConfig with Redis with UserService

  private def create(config: HooklaConfig, redis: Redis, userService: UserService) = new AuthServiceImpl(config, redis, userService)

  val live: ZLayer[In, Throwable, AuthService] = ZLayer.fromFunction(create _)
  def apply(): URIO[AuthService, AuthService]  = ZIO.service[AuthService]
}
