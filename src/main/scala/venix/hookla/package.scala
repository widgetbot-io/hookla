package venix

import io.getquill.SnakeCase
import io.getquill.context.zio.PostgresZioJAsyncContext

package object hookla {
  object QuillContext extends PostgresZioJAsyncContext(SnakeCase)
}
