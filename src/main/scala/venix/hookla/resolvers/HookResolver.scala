package venix.hookla.resolvers

import venix.hookla.Result
import venix.hookla.entities.Hook
import venix.hookla.services.db.IHookService
import venix.hookla.types.{HookId, TeamId}
import zio.{ZIO, ZLayer}

trait IHookResolver {
  def get(team: TeamId, hook: HookId): Result[Option[Hook]]
  def getByTeam(team: TeamId): Result[List[Hook]]
}

class HookResolver(
    private val hookService: IHookService
) extends IHookResolver {
  def get(team: TeamId, hook: HookId): Result[Option[Hook]] = ???
  def getByTeam(team: TeamId): Result[List[Hook]]           = ???
}

object HookResolver {
  private type In = IHookService
  private def create(a: IHookService) = new HookResolver(a)

  val live: ZLayer[In, Throwable, IHookResolver] = ZLayer.fromFunction(create _)
  def apply()                                    = ZIO.service[IHookResolver]
}
