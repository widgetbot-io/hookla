package venix.hookla.resolvers

import venix.hookla.Result
import venix.hookla.entities.{Hook, HookSink, Sink, Team}
import venix.hookla.services.db.HookService
import venix.hookla.types.{HookId, TeamId}
import zio.{ZIO, ZLayer}

trait HookResolver {
  def resolveTeam(hook: Hook): Result[Team]
  def resolveSinks(hook: Hook): Result[List[HookSink]]
}

private class HookResolverImpl(
    private val hookService: HookService
) extends HookResolver {
  def resolveTeam(hook: Hook): Result[Team]            = ???
  def resolveSinks(hook: Hook): Result[List[HookSink]] = ???
}

object HookResolver {
  private type In = HookService
  private def create(a: HookService) = new HookResolverImpl(a)

  val live: ZLayer[In, Throwable, HookResolver] = ZLayer.fromFunction(create _)
  def apply()                                   = ZIO.service[HookResolver]
}
