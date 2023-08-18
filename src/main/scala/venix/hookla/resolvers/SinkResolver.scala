package venix.hookla.resolvers

import venix.hookla.Result
import venix.hookla.entities.{Hook, HookSink, Sink}
import venix.hookla.services.db.HookService
import venix.hookla.types.HookId
import zio.{UIO, ZIO, ZLayer}

trait SinkResolver {
  def getAll: Result[List[Sink]]

  def resolveHook(sink: HookSink): Result[Hook]
}

private class SinkResolverImpl(
    private val hookService: HookService
) extends SinkResolver {
  def getAll: Result[List[Sink]] = ZIO.succeed(Nil)

  def resolveHook(sink: HookSink): Result[Hook] = hookService.getUnsafe(HookId(sink.hookId)).map(_.toEntity)
}

object SinkResolver {
  private type In = HookService
  private def create(a: HookService) = new SinkResolverImpl(a)

  val live: ZLayer[In, Throwable, SinkResolver] = ZLayer.fromFunction(create _)
}
