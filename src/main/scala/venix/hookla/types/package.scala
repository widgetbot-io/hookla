package venix.hookla

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
    def wrap[FROM, TO](a: FROM)(implicit equiv: Equivalence[FROM, TO]): TO   = implicitly[Equivalence[FROM, TO]].to(a)
    def unwrap[FROM, TO](a: TO)(implicit equiv: Equivalence[FROM, TO]): FROM = implicitly[Equivalence[FROM, TO]].from(a)
  }

  object UserId extends RichNewtype[UUID]
  type UserId = UserId.Type

  object TeamId extends RichNewtype[UUID]
  type TeamId = TeamId.Type

  object TeamUserId extends RichNewtype[UUID]
  type TeamUserId = TeamUserId.Type

  object HookId extends RichNewtype[UUID]
  type HookId = HookId.Type

  object HookSinkId extends RichNewtype[UUID]
  type HookSinkId = HookSinkId.Type

  object HookSettingsId extends RichNewtype[UUID]
  type HookSettingsId = HookSettingsId.Type
}
