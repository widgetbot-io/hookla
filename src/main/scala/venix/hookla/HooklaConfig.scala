package venix.hookla

case class HooklaConfig (
  environment: String,
  app: AppConfig,
  postgres: PostgresConfig,
  flywayConfig: FlywayConfig,
)

case class AppConfig(
  port: Int
)

case class PostgresConfig(
  url: String,
  connectionTimeout: Int
)

case class FlywayConfig(
  url: String,
  user: String,
  password: String
)
