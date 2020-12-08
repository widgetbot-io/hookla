package venix.hookla.util.play

import java.lang.reflect.Method

import akka.actor._
import akka.actor.typed.Behavior
import akka.annotation.ApiMayChange
import com.google.inject._
import com.google.inject.assistedinject.FactoryModuleBuilder
import venix.hookla.util.play.TypedAkka._

import scala.reflect._

trait AkkaGuiceSupport {
  self: AbstractModule =>

  import com.google.inject.name.Names
  import com.google.inject.util.Providers

  private def accessBinder: Binder = {
    val method: Method = classOf[AbstractModule].getDeclaredMethod("binder")
    if (!method.isAccessible) {
      method.setAccessible(true)
    }
    method.invoke(this).asInstanceOf[Binder]
  }

  def bindActor[T <: Actor: ClassTag](name: String, props: Props => Props = identity): Unit = {
    accessBinder
      .bind(classOf[ActorRef])
      .annotatedWith(Names.named(name))
      .toProvider(Providers.guicify(Akka.providerOf[T](name, props)))
      .asEagerSingleton()
  }

  def bindActorFactory[ActorClass <: Actor: ClassTag, FactoryClass: ClassTag]: Unit = {
    accessBinder.install(
      new FactoryModuleBuilder()
        .implement(classOf[Actor], implicitly[ClassTag[ActorClass]].runtimeClass.asInstanceOf[Class[_ <: Actor]])
        .build(implicitly[ClassTag[FactoryClass]].runtimeClass)
    )
  }

  @ApiMayChange
  final def bindTypedActor[T: ClassTag](behavior: Behavior[T], name: String): Unit = {
    accessBinder.bind(behaviorOf[T]).toInstance(behavior)
    bindTypedActorRef[T](name)
  }

  @ApiMayChange
  final def bindTypedActor[T: ClassTag](actorModule: ActorModule.Aux[T], name: String): Unit = {
    accessBinder.install(actorModule)
    bindTypedActorRef[T](name)
  }

  private final def bindTypedActorRef[T: ClassTag](name: String): Unit = {
    accessBinder.bind(actorRefOf[T]).toProvider(new TypedActorRefProvider[T](name)).asEagerSingleton()
  }
}
