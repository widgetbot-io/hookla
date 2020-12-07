package venix.hookla.actors

import akka.NotUsed
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

sealed trait EventHandlerEvent

sealed trait EventHandlerCommand
sealed trait GitlabEvent extends EventHandlerCommand
//sealed trait GithubEvent extends EventHandlerCommand

final case class PushEvent(eee: String) extends GitlabEvent
//final case class PipelineEvent(aaa: String) extends GitlabEvent

//final case class IssueEvent(eee: String) extends GithubEvent
//final case class EventToHandle(test: String) extends EventHandlerCommand

object EventHandler {
  def apply(gitlab: ActorRef[GitlabEvent]): Behavior[EventHandlerCommand] =
    Behaviors.setup(ctx => new EventHandlerBehaviour(ctx, gitlab))

  class EventHandlerBehaviour(context: ActorContext[EventHandlerCommand], gitlabActor: ActorRef[GitlabEvent]) extends AbstractBehavior[EventHandlerCommand](context) {
    override def onMessage(msg: EventHandlerCommand): Behavior[EventHandlerCommand] =
      msg match {
        case event: GitlabEvent =>
          gitlabActor ! event
          this
      }
  }
}

object GitlabEventHandler {
  def apply(): Behavior[GitlabEvent] =
    Behaviors.setup(ctx => new GitlabEventHandlerBehaviour(ctx))

  class GitlabEventHandlerBehaviour(context: ActorContext[GitlabEvent]) extends AbstractBehavior[GitlabEvent](context) {
    override def onMessage(e: GitlabEvent): Behavior[GitlabEvent] =
      e match {
        case PushEvent(eee) =>
          // make the embed
          // send to discord
          this
        //        case PipelineEvent(aaa) =>
        //          this
      }
  }
}

object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val gitlabEventHandler = ctx.spawn(GitlabEventHandler(), "gitlabEventHandler")
      val eventHandler = ctx.spawn(EventHandler(gitlabEventHandler), "eventHandler")
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit = {
    val as = ActorSystem(Main(), "Hookla")

    val test = as.systemActorOf[GitlabEvent]
//    test ! PushEvent("")
  }
}