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
import passtgen.passgen.PasswordGen

object Authenticator {

  def apply(): Behavior[Command] =
    AuthenticationProcesses()

  sealed trait Command
  case class AuthUser(
      email: String,
      genCommand: PasswordGen.Command,
      replyTo: ActorRef[PasswordGen.Command]
  ) extends Command

  sealed trait Response extends Command
  case class AuthUserResponse(
      maybeUser: Option[User],
      genCommand: PasswordGen.Command,
      replyTo: ActorRef[PasswordGen.Command]
  ) extends Response
  case class UserDatabaseFailure(
      exception: Throwable,
      replyTo: ActorRef[PasswordGen.Command]
  ) extends Response

  def AuthenticationProcesses(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case AuthUser(email, genCommand, replyTo) =>
          val dbUser: UserDB = UserDB(exCtx)
          context.pipeToSelf(dbUser.getUser(email)) {
            case Failure(ex)   => UserDatabaseFailure(ex, replyTo)
            case Success(user) => AuthUserResponse(user, genCommand, replyTo)
          }
          Behaviors.same
        case AuthUserResponse(maybeUser, genCommand, replyTo) =>
          maybeUser match {
            case None =>
              replyTo ! PasswordGen.AuthFailure(new Exception("NotFound"))
            case Some(user) =>
              replyTo ! PasswordGen.AuthSuccess(user.email, genCommand)
          }
          Behaviors.stopped
        case UserDatabaseFailure(ex, replyTo) =>
          replyTo ! PasswordGen.AuthFailure(ex)
          Behaviors.same
      }

    }
}
