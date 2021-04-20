package passtgen.passgen

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

object PasswordGen {
  def apply(): Behavior[Command] = GeneratorBeheavior()

  sealed trait Command
  case class GetPassword(
      email: String,
      parameters: Parameters
  ) extends Command
  case class GetPassphrase(
      email: String,
      parameters: Parameters
  ) extends Command
  case class GetPasswordResponse(maybePassword: String) extends Command
  case class GetPassphraseResponse(maybePassphrase: String) extends Command
  case class PasswordDatabaseFailure(exception: Throwable) extends Command

  def GeneratorBeheavior(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case GetPassphrase(email, parameters)       => ???
        case GetPassword(email, parameters)         => ???
        case GetPassphraseResponse(maybePassphrase) => ???
        case GetPasswordResponse(maybePassword)     => ???
        case PasswordDatabaseFailure(exception)     => ???
      }
    }
}
