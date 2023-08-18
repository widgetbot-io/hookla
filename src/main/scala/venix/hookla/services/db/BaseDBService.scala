package venix.hookla.services.db

import io.getquill.{EntityQuery, Quoted}

private[db] trait BaseDBService {
  import venix.hookla.QuillContext._

  type User     = venix.hookla.models.User
  type Team     = venix.hookla.models.Team
  type TeamUser = venix.hookla.models.TeamUser
  type Hook     = venix.hookla.models.Hook

  protected lazy val users: Quoted[EntityQuery[User]]         = quote(querySchema[User]("users"))
  protected lazy val teams: Quoted[EntityQuery[Team]]         = quote(querySchema[Team]("teams"))
  protected lazy val teamUsers: Quoted[EntityQuery[TeamUser]] = quote(querySchema[TeamUser]("team_users"))
  protected lazy val hooks: Quoted[EntityQuery[Hook]]         = quote(querySchema[Hook]("hooks"))
}
