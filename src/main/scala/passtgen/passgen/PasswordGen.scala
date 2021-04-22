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
import passtgen.auth.Authenticator
import passtgen.passgen.passphrase.word.WordDB
import passtgen.passgen.passphrase.Passphrase

object PasswordGen {
  def apply(): Behavior[Command] = GeneratorBeheavior()

  sealed trait Command
  case class GetPassword(
      parameters: Parameters*
  ) extends Command
  case class GetPassphrase(
      length: Length
  ) extends Command
  case class GetPasswordResponse(maybePassword: Option[String]) extends Command
  case class GetPassphraseResponse(maybePassphrase: String) extends Command
  case class PassphraseDatabaseFailure(exception: Throwable) extends Command

  case class Authentication(email: String, genCommand: Command) extends Command
  case class AuthFailure(exception: Throwable) extends Command

  def GeneratorBeheavior(): Behavior[Command] =
    Behaviors.receive[Command] { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case GetPassword(parameters) =>
          val pass = Password(parameters)
          context.self ! GetPasswordResponse(pass.generatePassword())
          Behaviors.same
        case GetPasswordResponse(maybePassword) =>
          Behaviors.same
        case GetPassphrase(length) =>
          val passphrase = Passphrase(length)
          context.pipeToSelf(passphrase.generatePassphrase) {
            case Failure(ex)         => PassphraseDatabaseFailure(ex)
            case Success(passphrase) => GetPassphraseResponse(passphrase)
          }
          Behaviors.same
        case GetPassphraseResponse(maybePassphrase) => ???
        case PassphraseDatabaseFailure(ex) =>
          context.log.error(ex.getMessage())
          Behaviors.same
        case Authentication(email, genCommand) =>
          val authenticator = context.spawn(Authenticator(), s"auth-$email")
          authenticator ! Authenticator.AuthUser(
            email,
            genCommand,
            context.self
          )
          Behaviors.same
        case AuthFailure(ex) =>
          context.log.error(ex.getMessage())
          Behaviors.same
      }
    }
}
