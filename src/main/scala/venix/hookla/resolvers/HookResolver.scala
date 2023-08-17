package venix.hookla.resolvers

import venix.hookla.Result
import venix.hookla.entities.{Hook, HookSink, Sink, Team}
import venix.hookla.services.db.IHookService
import venix.hookla.types.{HookId, TeamId}
import zio.{ZIO, ZLayer}

trait IHookResolver {
  def resolveTeam(hook: Hook): Result[Team]
  def resolveSinks(hook: Hook): Result[List[HookSink]]
}

class HookResolver(
    private val hookService: IHookService
) extends IHookResolver {
  def resolveTeam(hook: Hook): Result[Team]            = ???
  def resolveSinks(hook: Hook): Result[List[HookSink]] = ???
}

object HookResolver {
  private type In = IHookService
  private def create(a: IHookService) = new HookResolver(a)

  val live: ZLayer[In, Throwable, IHookResolver] = ZLayer.fromFunction(create _)
  def apply()                                    = ZIO.service[IHookResolver]
}
