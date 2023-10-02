package venix.hookla

import caliban.ZHttpAdapter
import caliban.interop.tapir.HttpInterpreter
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import io.circe.Json
import io.getquill.context.zio._
import io.getquill.util.LoadConfig
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.tapir.json.circe._
import venix.hookla.RequestError.{BadRequest, Forbidden, Unauthenticated}
import venix.hookla.http.Auth
import venix.hookla.resolvers._
import venix.hookla.services.core._
import venix.hookla.services.db._
import venix.hookla.services.http.DiscordUserService
import venix.hookla.sources.WebhookController
import zio._
import zio.http._
import zio.logging.backend.SLF4J
import zio.redis.{CodecSupplier, Redis, RedisConfig, RedisExecutor}

import scala.language.postfixOps

object App extends ZIOAppDefault {
  // Allows zio-logging to use slf4j (logback)
  private val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val app: ZIO[Env, Throwable, Unit] = for {
    _                <- ZIO.logInfo("Starting Hookla!")
    migrationService <- ZIO.service[FlywayMigrationService]
//    _                <- migrationService.migrate().orDie

    webhookController <- ZIO.service[WebhookController]
    schemaResolver    <- ZIO.service[SchemaResolver]
    api = schemaResolver.graphQL

    apiInterpreter <- api.interpreter
    app = Http
      .collectHttp[Request] {
        case Method.POST -> !! / "api" / "v1" / "webhooks" / webhookId =>
          webhookController.makeHttpService
        case _ -> !! / "api" / "graphql" =>
          ZHttpAdapter.makeHttpService(HttpInterpreter(apiInterpreter)) @@ Auth.middleware
      }
      .tapErrorCauseZIO(cause => ZIO.logErrorCause(cause))
      .mapError {
        case e: Unauthenticated => Response(status = Status.Unauthorized, body = Body.fromString(Json.obj("error" -> Json.fromString(e.message)).spaces2))
        case e: Forbidden       => Response(status = Status.Forbidden, body = Body.fromString(Json.obj("error" -> Json.fromString(e.message)).spaces2))
        case e: BadRequest      => Response(status = Status.BadRequest, body = Body.fromString(Json.obj("error" -> Json.fromString(e.message)).spaces2))
        case _                  => Response(status = Status.InternalServerError)
      }

    _ <- ZIO.logInfo("Starting GraphQL Server on ::8443")
    _ <- Server.serve[Env](app).forever.unit
  } yield ()

  val addSimpleLogger: ZLayer[Any, Nothing, Unit] =
    Runtime.addLogger((_, _, _, message: () => Any, _, _, _, _) => println(message()))

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    addSimpleLogger

  private val quillConfig: JAsyncContextConfig[PostgreSQLConnection] = PostgresJAsyncContextConfig(LoadConfig("postgres"))
  override def run =
    app
      .provide(
        // Bloody redis
        Redis.layer,
        RedisExecutor.layer,
        ZLayer.succeed(RedisConfig.Default), // TODO: Make this configurable
        ZLayer.succeed(CodecSupplier.utf8string),
        /// End bloody redis
        ZLayer.fromZIO(HooklaConfig()),
        ZLayer.succeed(quillConfig) >>> ZioJAsyncConnection.live[PostgreSQLConnection],
        HttpClientZioBackend.layer(),
        Auth.http,
        HTTPService.live,
        FlywayMigrationService.live,
        DiscordUserService.live,
        SinkResolver.live,
        SourceResolver.live,
        SchemaResolver.live,
        UserResolver.live,
        UserService.live,
        TeamService.live,
        AuthService.live,
        HookResolver.live,
        HookService.live,
        TeamResolver.live,
        WebhookController.live,
        // zhttp server config
        Server.defaultWithPort(8443),
        logger
//      ZLayer.Debug.mermaid,
      )
      .tapErrorCause(cause => ZIO.logInfoCause(cause))
      .exitCode
}
