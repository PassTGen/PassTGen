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
  def apply(): Behavior[Command] =
    authenticator()

  sealed trait Command
  case class GetPassword(
      replyTo: ActorRef[Command],
      parameters: Seq[GeneratorParameters]
  ) extends Command
  case class GetPassphrase(
      length: Length,
      replyTo: ActorRef[Command]
  ) extends Command
  case class GetPasswordResponse(
      maybePassword: String,
      replyTo: ActorRef[Command]
  ) extends Command
  case class GetPassphraseResponse(
      maybePassphrase: String,
      replyTo: ActorRef[Command]
  ) extends Command
  case class PassphraseDatabaseFailure(
      exception: String,
      replyTo: ActorRef[Command]
  ) extends Command

  case class Generate(
      email: String,
      genCommand: String,
      genParameters: Seq[GeneratorParameters],
      replyTo: ActorRef[Command]
  ) extends Command
  case class AuthSuccess(
      email: String,
      genCommand: String,
      genParameters: Seq[GeneratorParameters],
      replyTo: ActorRef[Command]
  ) extends Command
  case class AuthFailure(exception: String, replyTo: ActorRef[Command])
      extends Command

  final case class PasswordGeneratedOK(password: String) extends Command
  final case class DatabaseFailure(reason: String) extends Command
  final case class AuthUserFailure(reason: String) extends Command

  def authenticator(): Behavior[Command] =
    Behaviors.receive[Command] { (context, message) =>
      message match {
        case Generate(email, genCommand, genParameters, replyTo) =>
          val authenticator = context.spawn(Authenticator(), s"auth-$email")
          authenticator ! Authenticator.AuthUser(
            email,
            genCommand,
            genParameters,
            context.self
          )
          Behaviors.same
        case AuthSuccess(email, genCommand, genParameters, replyTo) =>
          val generator =
            context.spawn(generatorBeheavior(), s"gen-$email")
          generator ! (genCommand match {
            case "password" => GetPassword(replyTo, genParameters)
            case "passphrase" =>
              GetPassphrase(genParameters.head.asInstanceOf[Length], replyTo)
          })
          Behaviors.same
        case AuthFailure(ex, replyTo) =>
          replyTo ! AuthUserFailure(ex)
          Behaviors.same
      }
    }

  def generatorBeheavior(): Behavior[Command] =
    Behaviors.receive[Command] { (context, message) =>
      message match {
        case GetPassword(replyTo, parameters) =>
          val pass = Password(parameters)
          context.self ! GetPasswordResponse(pass.generatePassword(), replyTo)
          Behaviors.same
        case GetPasswordResponse(maybePassword, replyTo) =>
          replyTo ! PasswordGeneratedOK(maybePassword)
          Behaviors.same
        case GetPassphrase(length, replyTo) =>
          implicit val exCtx = context.executionContext
          val passphrase = Passphrase(length)
          context.pipeToSelf(passphrase.generatePassphrase) {
            case Failure(ex) =>
              PassphraseDatabaseFailure(ex.getMessage(), replyTo)
            case Success(passphrase) =>
              GetPassphraseResponse(passphrase, replyTo)
          }
          Behaviors.same
        case GetPassphraseResponse(maybePassphrase, replyTo) =>
          replyTo ! PasswordGeneratedOK(maybePassphrase)
          Behaviors.same
        case PassphraseDatabaseFailure(ex, replyTo) =>
          replyTo ! DatabaseFailure(ex)
          Behaviors.same
      }
    }
}
