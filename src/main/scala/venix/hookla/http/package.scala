package venix.hookla

import venix.hookla.RequestError.{Unauthenticated, Unauthorized}
import venix.hookla.models.User
import venix.hookla.services.core.AuthService
import zio.http.Header.Authorization.render
import zio.http.{Header, HttpAppMiddleware}
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
              case None       => ZIO.fail(Unauthenticated("You must be logged in to perform this action!!"))
            }
            def authenticate(token: String): Task[Unit] = AuthService().flatMap(svc => svc.decodeToken(token).flatMap(setUser)).unit
          }
        }
    }

    val middleware = HttpAppMiddleware.customAuthZIO { headers =>
      headers.get(Header.Authorization) match {
        case Some(Header.Authorization.Bearer(token)) => ZIO.serviceWithZIO[Auth](_.authenticate(token)).as(true)
        case _                                        => ZIO.fail(Unauthenticated("You must be logged in to perform this action!!!"))
      }
    }

    def currentUser: ZIO[Auth, RequestError, User] = ZIO.serviceWithZIO[Auth](_.currentUser)
  }
}
