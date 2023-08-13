package venix.hookla

import caliban.interop.tapir.HttpInterpreter
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{printErrors, timeout}
import caliban.{CalibanError, ZHttpAdapter, graphQL}
import io.getquill.SnakeCase
import io.getquill.context.zio.PostgresZioJAsyncContext
import sttp.tapir.json.circe._
import venix.hookla.resolvers._
import venix.hookla.services.db._
import zio._
import zio.http._
import zio.logging.backend.SLF4J

import scala.language.postfixOps

object App extends ZIOAppDefault {
  object QuillContext extends PostgresZioJAsyncContext(SnakeCase)

  import caliban.schema.Schema.auto._

  // Allows zio-logging to use slf4j (logback)
  private val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val app: ZIO[Any with Server with SchemaResolver with FlywayMigrationService, CalibanError.ValidationError, Unit] = for {
    migrationService <- ZIO.service[FlywayMigrationService]
    _                <- migrationService.migrate().orDie

    schemaResolver <- ZIO.service[SchemaResolver]
    rootResolver   <- schemaResolver.rootResolver
    api = graphQL(rootResolver) @@ printErrors @@ timeout(3 seconds) @@ apolloTracing
    apiInterpreter <- api.interpreter.map(_.mapError(_ => new Throwable("Error")))
    interpreter = apiInterpreter
    app: App[Any] = Http
      .collectHttp[Request] { case _ -> !! / "api" / "graphql" =>
        ZHttpAdapter.makeHttpService(HttpInterpreter(interpreter))
      }
      .tapErrorCauseZIO(cause => ZIO.succeed(println(cause.prettyPrint)))
      .withDefaultErrorResponse
    _ <- Server.serve[Any](app).forever.unit // Causes issues if left as Nothing
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    app
      .provide(
        ZLayer.fromZIO(HooklaConfig()),
        FlywayMigrationService.live,
        SinkResolver.live,
        SourceResolver.live,
        SchemaResolver.live,
        // zhttp server config
        Server.defaultWithPort(8443),
        logger
//      ZLayer.Debug.mermaid,
      )
      .exitCode
}
