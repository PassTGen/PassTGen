package passtgen.auth

import user.User

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.util.Timeout
import scala.concurrent.duration._
import passtgen.auth.user.UserDB
import scala.util.Failure
import akka.actor.Status
import scala.util.Success
//TODO: REFACTOR THIS TO PIPE PATTERN
object Authenticator {

  def apply(): Behavior[Command] =
    Behaviors.setup(context => new Authenticator(context))

  sealed trait Command
  case class CreateUser(
      email: String,
      replyTo: ActorRef[CreateUserResponse]
  ) extends Command
  case class GetUser(
      email: String,
      replyTo: ActorRef[GetUserResponse]
  ) extends Command

  sealed trait Reply extends Command
  case class GetUserResponse(maybeUser: Option[User]) extends Reply
  case class CreateUserResponse(maybeUser: Option[User]) extends Reply

  def AuthenticationProcesses(user: User): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case CreateUser(email, replyTo) =>
          val dbUser = UserDB(exCtx)
          dbUser.createUser(email).onComplete {
            case Failure(_)    => replyTo ! CreateUserResponse(None)
            case Success(user) => replyTo ! CreateUserResponse(Some(user))
          }
          Behaviors.same
        case GetUser(email, replyTo) =>
          val dbUser: UserDB = UserDB(exCtx)
          dbUser.getUser(email).onComplete {
            case Failure(_)    => replyTo ! GetUserResponse(None)
            case Success(user) => replyTo ! GetUserResponse(user)
          }
          Behaviors.same
        case GetUserResponse(maybeUser) =>
          Behaviors.ignore
        case CreateUserResponse(maybeUser) =>
          Behaviors.ignore
      }

    }
}
class Authenticator(context: ActorContext[Authenticator.Command])
    extends AbstractBehavior[Authenticator.Command](context) {
  import Authenticator._
  override def onMessage(
      msg: Authenticator.Command
  ): Behavior[Authenticator.Command] =
    msg match {
      case CreateUser(email, replyTo) =>
        val dbActor = context.spawn(DbManager(), "CreateUserActorDb")
        dbActor ! DbManager.CreateUser(email, context.self)
        Behaviors.same
      case GetUser(email, replyTo) =>
        val dbActor = context.spawn(DbManager(), "GetUserActorDb")
        dbActor ! DbManager.GetUser(email, context.self)
        Behaviors.same

      case GetUserResponse(maybeUser) =>
        Behaviors.ignore
      case CreateUserResponse(maybeUser) =>
        Behaviors.ignore
    }
}
