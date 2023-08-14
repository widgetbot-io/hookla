package venix

import io.getquill.{MappedEncoding, SnakeCase}
import io.getquill.context.zio.PostgresZioJAsyncContext
import venix.hookla.types.RichNewtype
import zio.prelude.Equivalence

package object hookla {
  implicit def newtypeEncoder[A, T <: RichNewtype[A]#Type](implicit equiv: Equivalence[A, T]): MappedEncoding[T, A] =
    MappedEncoding[T, A](RichNewtype.unwrap(_))

  implicit def newtypeDecoder[A, T <: RichNewtype[A]#Type](implicit equiv: Equivalence[A, T]): MappedEncoding[A, T] =
    MappedEncoding[A, T](RichNewtype.wrap(_))

  object QuillContext extends PostgresZioJAsyncContext(SnakeCase)
}
