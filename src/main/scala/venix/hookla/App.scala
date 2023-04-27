package venix.hookla

import io.getquill.SnakeCase
import io.getquill.context.zio.PostgresZioJAsyncContext
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object App extends ZIOAppDefault {
  object QuillContext extends PostgresZioJAsyncContext(SnakeCase)

  private val app = for {
    _ <- ZIO.succeed(println("Hello, world!"))
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    app.provide(
      ZLayer.Debug.mermaid
    )
}
