package passtgen.auth

import user.User

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import passtgen.db.DbManagerActor
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.util.Timeout
import scala.concurrent.duration._

object AuthActor {

  def apply(): Behavior[Command] =
    Behaviors.setup(context => new AuthActor(context))

  sealed trait Command
  final case class CreateUser(
      email: String,
      replyTo: ActorRef[CreateUser]
  ) extends Command
  final case class GetUser(
      email: String,
      replyTo: ActorRef[GetUserResponse]
  ) extends Command

  sealed trait Reply extends Command
  final case class GetUserResponse(maybeUser: Option[User]) extends Reply
  final case class CreateUserResponse(maybeUser: Option[User]) extends Reply

}
class AuthActor(context: ActorContext[AuthActor.Command])
    extends AbstractBehavior[AuthActor.Command](context) {
  import AuthActor._
  override def onMessage(msg: AuthActor.Command): Behavior[AuthActor.Command] =
    msg match {
      case CreateUser(email, replyTo) =>
        val dbActor = context.spawn(DbManagerActor(), "CreateUserActorDb")
        dbActor ! DbManagerActor.CreateUser(email, context.self)
        Behaviors.same
      case GetUser(email, replyTo) =>
        val dbActor = context.spawn(DbManagerActor(), "GetUserActorDb")
        dbActor ! DbManagerActor.GetUser(email, context.self)
        Behaviors.same

      case GetUserResponse(maybeUser) =>
        Behaviors.ignore
      case CreateUserResponse(maybeUser) =>
        Behaviors.ignore
    }
}
