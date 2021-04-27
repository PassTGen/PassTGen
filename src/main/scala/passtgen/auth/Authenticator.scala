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
import passtgen.passgen.GeneratorParameters
import org.xbill.DNS.utils.base64
import akka.actor.Actor

object Authenticator {

  def apply(): Behavior[Command] =
    AuthenticationProcesses()

  sealed trait Command
  case class AuthUser(
      email: String,
      genCommand: String,
      genParameters: Seq[GeneratorParameters],
      replyTo: ActorRef[PasswordGen.Command],
      context: ActorRef[PasswordGen.Command]
  ) extends Command

  sealed trait Response extends Command
  case class AuthUserResponse(
      maybeUser: Option[User],
      genCommand: String,
      genParameters: Seq[GeneratorParameters],
      replyTo: ActorRef[PasswordGen.Command],
      context: ActorRef[PasswordGen.Command]
  ) extends Response
  case class UserDatabaseFailure(
      exception: String,
      replyTo: ActorRef[PasswordGen.Command],
      context: ActorRef[PasswordGen.Command]
  ) extends Response

  def AuthenticationProcesses(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val exCtx = context.executionContext
      message match {
        case AuthUser(
              email,
              genCommand,
              genParameters,
              replyTo,
              actorContext
            ) =>
          val dbUser: UserDB = UserDB(exCtx)
          val decryptEmail =
            new String(java.util.Base64.getDecoder.decode(email))
          context.pipeToSelf(dbUser.getUser(decryptEmail)) {
            case Failure(ex) =>
              UserDatabaseFailure(ex.getMessage(), replyTo, actorContext)
            case Success(user) =>
              AuthUserResponse(
                user,
                genCommand,
                genParameters,
                replyTo,
                actorContext
              )
          }
          Behaviors.same
        case AuthUserResponse(
              maybeUser,
              genCommand,
              genParameters,
              replyTo,
              actorContext
            ) =>
          maybeUser match {
            case None =>
              actorContext ! PasswordGen.AuthFailure(
                "Not Found",
                replyTo
              )
            case Some(user) =>
              actorContext ! PasswordGen.AuthSuccess(
                user.email,
                genCommand,
                genParameters,
                replyTo
              )
          }
          Behaviors.same
        case UserDatabaseFailure(ex, replyTo, actorContext) =>
          actorContext ! PasswordGen.AuthFailure(ex, replyTo)
          Behaviors.same
      }

    }
}
