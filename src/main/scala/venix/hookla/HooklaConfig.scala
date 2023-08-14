package venix.hookla

import io.circe.generic.auto._
import io.circe.config.parser
import zio.{UIO, ZIO}

case class HooklaConfig(
    environment: String,
    app: AppConfig,
    postgres: PostgresConfig,
    flywayConfig: FlywayConfig,
    discord: DiscordConfig
)

case class AppConfig(
    port: Int
)

case class PostgresConfig(
    url: String,
    connectionTimeout: Int
)

case class DiscordConfig(
    token: String
)

case class FlywayConfig(
    url: String,
    user: String,
    password: String
)

object HooklaConfig {
  def apply(): UIO[HooklaConfig] = ZIO.fromEither(parser.decode[HooklaConfig]()).orDie

  def pure(): HooklaConfig =
    parser.decode[HooklaConfig]().fold(errors => throw errors.fillInStackTrace(), identity)
}
