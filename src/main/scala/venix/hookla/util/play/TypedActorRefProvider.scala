package venix.hookla.util.play

import javax.inject.Singleton

import scala.reflect.ClassTag

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.actor.ActorSystem
import akka.annotation.ApiMayChange
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Provider
import venix.hookla.util.play.TypedAkka._

/**
 * A singleton [[Provider]] of the typed `ActorRef[T]` resulting from spawning an actor with the
 * `Behavior[T]` in dependency scope and the given name, in the [[ActorSystem]] in dependency scope.
 *
 * @param name the name to use when spawning the typed actor.
 * @tparam T The class of the messages the typed actor can handle.
 */
@Singleton
@ApiMayChange
final class TypedActorRefProvider[T: ClassTag](val name: String) extends Provider[ActorRef[T]] {
  @Inject private var actorSystem: ActorSystem = _
  @Inject private var guiceInjector: Injector  = _

  lazy val get = {
    val behavior = guiceInjector.getInstance(Key.get(behaviorOf[T]))
    actorSystem.spawn(behavior, name)
  }
}