package venix.hookla.util.play

import scala.reflect.ClassTag

trait Injector {
  def instanceOf[T: ClassTag]: T
  def instanceOf[T](clazz: Class[T]): T
}

object NewInstanceInjector extends Injector {
  def instanceOf[T](implicit ct: ClassTag[T]) = instanceOf(ct.runtimeClass.asInstanceOf[Class[T]])
  def instanceOf[T](clazz: Class[T]) = clazz.getDeclaredConstructor().newInstance()
}

class SimpleInjector(fallback: Injector, components: Map[Class[_], Any] = Map.empty) extends Injector {
  def instanceOf[T](implicit ct: ClassTag[T]) = instanceOf(ct.runtimeClass.asInstanceOf[Class[T]])
  def instanceOf[T](clazz: Class[T]) = components.getOrElse(clazz, fallback.instanceOf(clazz)).asInstanceOf[T]
  def +[T](component: T)(implicit ct: ClassTag[T]): SimpleInjector =
    new SimpleInjector(fallback, components + (ct.runtimeClass -> component))
  def add[T](clazz: Class[T], component: T): SimpleInjector =
    new SimpleInjector(fallback, components + (clazz -> component))
}

private[play] class ContextClassLoaderInjector(delegate: Injector, classLoader: ClassLoader) extends Injector {
  override def instanceOf[T: ClassTag]: T           = withContext { delegate.instanceOf[T] }
  override def instanceOf[T](clazz: Class[T]): T    = withContext { delegate.instanceOf(clazz) }

  @inline
  private def withContext[T](body: => T): T = {
    val thread         = Thread.currentThread()
    val oldClassLoader = thread.getContextClassLoader
    thread.setContextClassLoader(classLoader)
    try body
    finally thread.setContextClassLoader(oldClassLoader)
  }
}