package venix.hookla.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object Discord {
  sealed trait Command

  final case class SendEmbedToDiscord(eee: String) extends Command
}

object DiscordMessageSender {
  def apply(): Behavior[Discord.Command] =
    Behaviors.setup(ctx => new DiscordMessageSenderBehaviour(ctx))

  class DiscordMessageSenderBehaviour(context: ActorContext[Discord.Command]) extends AbstractBehavior[Discord.Command](context) {
    import Discord._

    override def onMessage(e: Command): Behavior[Command] =
      e match {
        case SendEmbedToDiscord(eee) =>
          println(s"aaaaa: ${eee}")
          // send to discord
          this
      }
  }
}