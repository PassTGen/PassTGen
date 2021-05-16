package passtgen.auth

import akka.actor.Status
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import passtgen.auth.user.UserDB
import passtgen.auth.user.User._
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import passtgen.passgen.PassGen
import passtgen.auth.user.User

object Registration {
  def apply(): Behavior[Command] =
    registrationProcess()

  sealed trait Command
  case class CreateUser(email: String, replyTo: ActorRef[Command])
      extends Command

  sealed trait Response extends Command
  case class CreateUserResponse(maybeUser: User, replyTo: ActorRef[Command])
      extends Response
  case class UserDatabaseFailure(
      exception: Throwable,
      replyTo: ActorRef[Command]
  ) extends Response
  final case class OK(user: User) extends Response
  final case class DatabaseFailure(reason: String) extends Response

  def registrationProcess(): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      {
        implicit val exCtx = context.executionContext
        message match {
          case CreateUser(email, replyTo) =>
            val dbUser = UserDB(exCtx)
            context.pipeToSelf(dbUser.createUser(email)) {
              case Failure(ex)   => UserDatabaseFailure(ex, replyTo)
              case Success(user) => CreateUserResponse(user, replyTo)
            }
            Behaviors.same
          case CreateUserResponse(maybeUser, replyTo) =>
            replyTo ! OK(maybeUser)
            Behaviors.same
          case UserDatabaseFailure(ex, replyTo) =>
            replyTo ! DatabaseFailure(s"Database Failure: ${ex.getLocalizedMessage()}")
            Behaviors.same
        }
      }
    }
  }
}
