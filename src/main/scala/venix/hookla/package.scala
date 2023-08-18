package venix

import caliban.schema.Schema
import io.getquill.context.zio.{PostgresZioJAsyncContext, ZioJAsyncConnection}
import io.getquill.{MappedEncoding, SnakeCase}
import sttp.client3.httpclient.zio.SttpClient
import venix.hookla.http.Auth
import venix.hookla.resolvers._
import venix.hookla.services.core.{AuthService, HTTPService}
import venix.hookla.services.db.{FlywayMigrationService, HookService, UserService}
import venix.hookla.services.http.DiscordUserService
import venix.hookla.types.RichNewtype
import zio._
import zio.http.Server
import zio.prelude.Equivalence
import zio.redis.Redis

package object hookla {
  implicit def newtypeEncoder[A, T <: RichNewtype[A]#Type](implicit equiv: Equivalence[A, T]): MappedEncoding[T, A] = MappedEncoding[T, A](RichNewtype.unwrap(_))
  implicit def newtypeDecoder[A, T <: RichNewtype[A]#Type](implicit equiv: Equivalence[A, T]): MappedEncoding[A, T] = MappedEncoding[A, T](RichNewtype.wrap(_))

  object QuillContext extends PostgresZioJAsyncContext(SnakeCase)

  type Env = HooklaConfig with ZioJAsyncConnection with Redis with SttpClient with Auth with IUserResolver with IHookResolver with HTTPService with FlywayMigrationService with DiscordUserService with ISinkResolver with ISourceResolver with ISchemaResolver with IUserResolver with UserService with HookService with AuthService with Server

  type Result[T]       = IO[RequestError, T]
  type ResultOpt[T]    = IO[RequestError, Option[T]]
  type ResultList[T]   = IO[RequestError, List[T]]
  type Task[T]         = ZIO[Env, RequestError, T]
  type TaskOpt[T]      = ZIO[Env, RequestError, Option[T]]
  type TaskList[T]     = ZIO[Env, RequestError, List[T]]
  type CustomSchema[T] = Schema[Env, T]
}
