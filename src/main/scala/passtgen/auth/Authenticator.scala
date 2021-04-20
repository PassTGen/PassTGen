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

object Authenticator {

  def apply(): Behavior[Command] =
    AuthenticationProcesses()

  sealed trait Command
  case class CreateUser(
      email: String
  ) extends Command
  case class GetUser(
      email: String
  ) extends Command

  case class GetUserResponse(maybeUser: Option[User]) extends Command
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
        case GetUser(email) =>
          val dbUser: UserDB = UserDB(exCtx)
          context.pipeToSelf(dbUser.getUser(email)) {
            case Failure(ex)   => UserDatabaseFailure(ex)
            case Success(user) => GetUserResponse(user)
          }
          Behaviors.same
        case GetUserResponse(maybeUser) =>
          Behaviors.same
        case CreateUserResponse(maybeUser) =>
          Behaviors.same
        case UserDatabaseFailure(ex) =>
          context.log.error(ex.getMessage())
          Behaviors.same
      }

    }
}
