package venix.hookla.resolvers

import venix.hookla.Result
import venix.hookla.entities.{Hook, HookSink, Sink}
import venix.hookla.services.db.IHookService
import venix.hookla.types.HookId
import zio.{UIO, ZIO, ZLayer}

trait ISinkResolver {
  def getAll: Result[List[Sink]]

  def resolveHook(sink: HookSink): Result[Hook]
}

class SinkResolver(
    private val hookService: IHookService
) extends ISinkResolver {
  def getAll: Result[List[Sink]] = ZIO.succeed(Nil)

  def resolveHook(sink: HookSink): Result[Hook] = hookService.getUnsafe(HookId(sink.hookId)).map(_.toEntity)
}

object SinkResolver {
  private type In = IHookService
  private def create(a: IHookService) = new SinkResolver(a)

  val live: ZLayer[In, Throwable, ISinkResolver] = ZLayer.fromFunction(create _)
}
