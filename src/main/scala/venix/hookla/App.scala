package venix.hookla

import caliban.interop.tapir.HttpInterpreter
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{printErrors, timeout}
import caliban.{CalibanError, ZHttpAdapter, graphQL}
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import io.getquill._
import io.getquill.context.zio._
import io.getquill.util.LoadConfig
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.tapir.json.circe._
import venix.hookla.RequestError.Env
import venix.hookla.resolvers._
import venix.hookla.services.core.HTTPService
import venix.hookla.services.db._
import venix.hookla.services.http.DiscordUserService
import zio._
import zio.http._
import zio.logging.backend.SLF4J

import scala.language.postfixOps

object App extends ZIOAppDefault {
  import caliban.schema.Schema.auto._

  // Allows zio-logging to use slf4j (logback)
  private val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val app = for {
    _                <- ZIO.logInfo("Starting Hookla!")
    _                <- ZIO.service[ZioJAsyncConnection]
    migrationService <- ZIO.service[IFlywayMigrationService]
//    _                <- migrationService.migrate().orDie

    schemaResolver <- ZIO.service[ISchemaResolver]
    rootResolver   <- schemaResolver.rootResolver
    api = graphQL(rootResolver) @@ printErrors @@ timeout(3 seconds) @@ apolloTracing

    apiInterpreter <- api.interpreter
    app = Http
      .collectHttp[Request] { case _ -> !! / "api" / "graphql" =>
        ZHttpAdapter.makeHttpService(HttpInterpreter(apiInterpreter))
      }
      .tapErrorCauseZIO(cause => ZIO.logErrorCause(cause))
      .withDefaultErrorResponse

    _ <- ZIO.logInfo("Starting GraphQL Server on ::8443")
    _ <- Server.serve[Any](app).forever.unit // Causes issues if left as Nothing
  } yield ()

  val addSimpleLogger: ZLayer[Any, Nothing, Unit] =
    Runtime.addLogger((_, _, _, message: () => Any, _, _, _, _) => println(message()))

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    addSimpleLogger

  private val quillConfig: JAsyncContextConfig[PostgreSQLConnection] = PostgresJAsyncContextConfig(LoadConfig("postgres"))
  override def run =
    app
      .provide(
        ZLayer.fromZIO(HooklaConfig()),
        ZLayer.succeed(quillConfig) >>> ZioJAsyncConnection.live[PostgreSQLConnection],
        HttpClientZioBackend.layer(),
        HTTPService.live,
        FlywayMigrationService.live,
        DiscordUserService.live,
        SinkResolver.live,
        SourceResolver.live,
        SchemaResolver.live,
        UserResolver.live,
        UserService.live,
        // zhttp server config
        Server.defaultWithPort(8443),
        logger
//      ZLayer.Debug.mermaid,
      )
      .tapErrorCause(cause => ZIO.logInfoCause(cause))
      .exitCode
}
