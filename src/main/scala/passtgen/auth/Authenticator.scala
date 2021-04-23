package passtgen.auth

import akka.actor.Status
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import passtgen.auth.user.UserDB
import passtgen.auth.user.User
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import passtgen.passgen.PasswordGen

object Authenticator {

  def apply(): Behavior[Command] =
    AuthenticationProcesses()

  sealed trait Command
  case class CreateUser(
      email: String
  ) extends Command
  case class AuthUser(
      email: String,
      genCommand: PasswordGen.Command,
      replyTo: ActorRef[PasswordGen.Command]
  ) extends Command

  case class AuthUserResponse(
      maybeUser: Option[User],
      genCommand: PasswordGen.Command,
      replyTo: ActorRef[PasswordGen.Command]
  ) extends Command
  case class CreateUserResponse(maybeUser: User) extends Command
  case class UserDatabaseFailure(exception: Throwable) extends Command

  def AuthenticationProcesses(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case CreateUser(email) =>
          val dbUser = UserDB(exCtx)
          context.pipeToSelf(dbUser.createUser(email)) {
            case Failure(ex)   => UserDatabaseFailure(ex)
            case Success(user) => CreateUserResponse(user)
          }
          Behaviors.same
        case AuthUser(email, genCommand, replyTo) =>
          val dbUser: UserDB = UserDB(exCtx)
          context.pipeToSelf(dbUser.getUser(email)) {
            case Failure(ex)   => UserDatabaseFailure(ex)
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
        case CreateUserResponse(maybeUser) =>
          Behaviors.same
        case UserDatabaseFailure(ex) =>
          context.log.error(ex.getMessage())
          Behaviors.same
      }

    }
}
