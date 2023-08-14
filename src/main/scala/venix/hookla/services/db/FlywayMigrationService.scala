package venix.hookla.services.db

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationState
import org.flywaydb.core.api.configuration.FluentConfiguration
import venix.hookla.{FlywayConfig, HooklaConfig}
import zio.{IO, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

trait IFlywayMigrationService {
  def migrate(): IO[Throwable, Unit]
}

class FlywayMigrationService(private val config: HooklaConfig) extends IFlywayMigrationService {
  private def logValidationErrorsIfAny(flywayConfig: FluentConfiguration): ZIO[Any, Throwable, Unit] =
    for {
      validated <- ZIO.succeed(flywayConfig.ignoreMigrationPatterns("*:pending").load().validateWithResult)
      _ <- ZIO.when(!validated.validationSuccessful)(
        ZIO.foreach(validated.invalidMigrations.asScala.toList)(e => ZIO.logError(s"Invalid migration: $e"))
      )
      _ <- ZIO.when(!validated.validationSuccessful)(
        ZIO.fail(new Error("Migrations validation failed (see the logs)"))
      )
    } yield ()

  private def migrationEffect(config: FlywayConfig): ZIO[Any, Throwable, Int] =
    for {

      flywayConfig <- ZIO.succeed(Flyway.configure.dataSource(config.url, config.user, config.password).group(true))
      _            <- logValidationErrorsIfAny(flywayConfig)
      _            <- ZIO.logInfo("Migrations validation successful")

      count <- ZIO.succeed(flywayConfig.load().migrate().migrationsExecuted)

      // fail for any statuses except success (in case of missing migration files, etc)
      _ <- ZIO.foreachDiscard(flywayConfig.load().info().all().toList) { i =>
        i.getState match {
          case MigrationState.SUCCESS => ZIO.unit
          case e                      => ZIO.fail(new Error(s"Migration ${i.getDescription} status is not 'SUCCESS': ${e.toString}"))
        }
      }

    } yield count

  def migrate(): IO[Throwable, Unit] =
    for {
      config <- ZIO.succeed(config.flywayConfig)
      _      <- ZIO.logInfo(s"Starting the migration for host: ${config.url}")
      count  <- migrationEffect(config)

//       _ <- ZIO.when(count < 1)(ZIO.fail(new Error("No migrations were executed")))
      _ <- ZIO.logInfo(s"Successful migrations: $count")
    } yield ()
}

object FlywayMigrationService {
  private type In = HooklaConfig
  private def create(config: HooklaConfig) = new FlywayMigrationService(config)

  val live: ZLayer[In, Throwable, IFlywayMigrationService] = ZLayer.fromFunction(create _)
}
