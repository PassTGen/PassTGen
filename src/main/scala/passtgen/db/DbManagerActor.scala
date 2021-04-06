package passtgen.db

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import user.User
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import passtgen.auth.AuthActor
import akka.actor.typed.Signal
import akka.actor.TypedActor
import akka.actor.typed.PostStop

object DbManagerActor {
  def apply(): Behavior[Command] =
    Behaviors.setup(context => new DbManagerActor(context))

  trait Command
  case class CreateUser(
      email: String,
      replyTo: ActorRef[AuthActor.Reply]
  ) extends Command
  case class GetUser(email: String, replyTo: ActorRef[AuthActor.Reply])
      extends Command
  case class GetWords(replyTo: ActorRef[GetWordsResponse]) extends Command

  case class CreateUserResponse(
      user: Option[User]
  )
  case class GetUserResponse(
      maybeUser: Option[User]
  )
  case class GetWordsResponse(
      words: List[String]
  )

}
class DbManagerActor(context: ActorContext[DbManagerActor.Command])
    extends AbstractBehavior[DbManagerActor.Command](context) {
  import DbManagerActor._
  override def onMessage(
      msg: DbManagerActor.Command
  ): Behavior[DbManagerActor.Command] =
    msg match {
      case CreateUser(email, replyTo) =>
        replyTo ! AuthActor.CreateUserResponse(User("3214123412", email))
        Behaviors.stopped
      case GetWords(replyTo) =>
        val words = List("Patata", "zanahoria", "cebolla")
        replyTo ! GetWordsResponse(words)
        Behaviors.stopped
      case GetUser(email, replyTo) =>
        replyTo ! AuthActor.CreateUserResponse(User("3214123412", email))
        Behaviors.stopped
    }
  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop => ???
  }
}
