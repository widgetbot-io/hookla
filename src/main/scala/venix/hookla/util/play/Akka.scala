package venix.hookla.util.play

import akka.actor.Actor
import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.BootstrapSetup
import akka.actor.ClassicActorSystemProvider
import akka.actor.CoordinatedShutdown
import akka.actor.Props
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import org.slf4j.LoggerFactory
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.util.Try

object Akka {
  def providerOf[T <: Actor: ClassTag](name: String, props: Props => Props = identity): Provider[ActorRef] =
    new ActorRefProvider(name, props)
}

class ActorRefProvider[T <: Actor: ClassTag](name: String, props: Props => Props) extends Provider[ActorRef] {
  @Inject private var actorSystem: ActorSystem = _
  @Inject private var injector: Injector = _

  lazy val get: ActorRef = {
    val creation = Props(injector.instanceOf[T])
    actorSystem.actorOf(props(creation), name)
  }
}