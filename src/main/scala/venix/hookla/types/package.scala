package venix.hookla

import caliban.schema.{ArgBuilder, Schema}
import io.circe.{Codec, Decoder, Encoder}
import zio.prelude._

import java.util.UUID

package object types {
  // Based on https://github.com/reibitto/podpodge/blob/6b8fc7e69c4f8e90a370fb538e1384bbede2e9f3/core/src/main/scala/podpodge/types/package.scala
  // Changed slightly as zio-prelude has changed since then
  abstract class RichNewtype[A: Encoder: Decoder] extends Newtype[A] { self =>
    implicit val equiv: A <=> Type = Equivalence(wrap, unwrap)

    implicit val encoder: Encoder[Type] = implicitly[Encoder[A]].contramap(unwrap)
    implicit val decoder: Decoder[Type] = implicitly[Decoder[A]].map(wrap)
    implicit val codec: Codec[Type]     = Codec.from(decoder, encoder)

    implicit final class UnwrapOps(value: Type) {
      def unwrap: A = self.unwrap(value)
    }

    def makeUnsafe(value: A): Type = make(value).fold(e => throw new IllegalArgumentException(e.mkString("; ")), identity)
  }

  object RichNewtype {
    def wrap[F, T](a: F)(implicit equiv: Equivalence[F, T]): T   = implicitly[Equivalence[F, T]].to(a)
    def unwrap[F, T](a: T)(implicit equiv: Equivalence[F, T]): F = implicitly[Equivalence[F, T]].from(a)
  }

  trait AsCoercible[A, B] {
    @inline final def apply(a: A): B = a.asInstanceOf[B]
  }

  object AsCoercible {
    def apply[A, B](implicit ev: AsCoercible[A, B]): AsCoercible[A, B] = ev
    def instance[A, B]: AsCoercible[A, B]                              = _instance.asInstanceOf[AsCoercible[A, B]]

    private val _instance = new AsCoercible[Any, Any] {}
  }

  trait Coercible[T] {
    type Type

    @inline implicit def wrapC: AsCoercible[T, Type]               = AsCoercible.instance
    @inline implicit def unwrapC: AsCoercible[Type, T]             = AsCoercible.instance
    @inline implicit def wrapM[M[_]]: AsCoercible[M[T], M[Type]]   = AsCoercible.instance
    @inline implicit def unwrapM[M[_]]: AsCoercible[M[Type], M[T]] = AsCoercible.instance
  }

  object UserId extends RichNewtype[UUID] with Coercible[UUID]
  type UserId = UserId.Type

  object TeamId extends RichNewtype[UUID] with Coercible[UUID]
  type TeamId = TeamId.Type

  object TeamUserId extends RichNewtype[UUID] with Coercible[UUID]
  type TeamUserId = TeamUserId.Type

  object HookId extends RichNewtype[UUID] with Coercible[UUID]
  type HookId = HookId.Type

  object HookSinkId extends RichNewtype[UUID] with Coercible[UUID]
  type HookSinkId = HookSinkId.Type

  object HookSettingsId extends RichNewtype[UUID] with Coercible[UUID]
  type HookSettingsId = HookSettingsId.Type
}
