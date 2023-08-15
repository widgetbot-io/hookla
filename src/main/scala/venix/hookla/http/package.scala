package venix.hookla

import venix.hookla.RequestError.{Unauthenticated, Unauthorized}
import venix.hookla.models.User
import zio.{FiberRef, IO, UIO, ULayer, ZIO, ZLayer}

package object http {
  trait Auth {
    def authenticate(token: String): Task[Unit]

    def currentUser: IO[RequestError, User]
  }

  object Auth {
    val http: ULayer[Auth] = ZLayer.scoped {
      FiberRef
        .make[Option[User]](None)
        .map { fiberRef =>
          new Auth {
            private def setUser(user: User): UIO[Unit] = fiberRef.set(Some(user))
            def currentUser: IO[RequestError, User] = fiberRef.get.flatMap {
              case Some(user) => ZIO.succeed(user)
              case None       => ZIO.fail(Unauthenticated("You must be logged in to perform this action"))
            }
            def authenticate(token: String): Task[Unit] = ZIO.succeed(???)
          }
        }
    }
  }
}
