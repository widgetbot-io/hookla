package venix.hookla.util.play

import akka.annotation.ApiMayChange
import com.google.inject.AbstractModule

@ApiMayChange
trait ActorModule extends AbstractModule {
  type Message
}

@ApiMayChange
object ActorModule {
  type Aux[A] = ActorModule { type Message = A }
}