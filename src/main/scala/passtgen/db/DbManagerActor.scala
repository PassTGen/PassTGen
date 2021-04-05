package passtgen.db

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import user.User
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import passtgen.db.DbManagerActor.CreateUser
import passtgen.db.DbManagerActor.GetPassphrase
import passtgen.db.DbManagerActor.GetPassphraseResponse
import passtgen.db.DbManagerActor.GetUser
import passtgen.auth.AuthActor

object DbManagerActor {
  def apply(): Behavior[Command] =
    Behaviors.setup(context => new DbManagerActor(context))

  sealed trait Command
  final case class CreateUser(user: User, replyTo: ActorRef[CreateUserResponse])
      extends Command
  final case class CreateUserResponse(replyTo: ActorRef[AuthActor.CreateUser])
      extends Command
  final case class GetUser(email: String, replyTo: ActorRef[GetUserResponse])
      extends Command
  final case class GetUserResponse(
      maybeUser: Option[User],
      replyTo: ActorRef[AuthActor.GetUserResponse]
  ) extends Command

  final case class GetPassphrase(replyTo: ActorRef[GetUserResponse])
      extends Command
  final case class GetPassphraseResponse(
      passphrase: String,
      replyTo: ActorRef[GetUserResponse]
  ) extends Command

}
class DbManagerActor(context: ActorContext[DbManagerActor.Command])
    extends AbstractBehavior[DbManagerActor.Command](context) {
  import DbManagerActor._
  override def onMessage(
      msg: Command
  ): Behavior[Command] =
    msg match {
      case CreateUser(user, replyTo)                  => ???
      case GetPassphrase(replyTo)                     => ???
      case GetPassphraseResponse(passphrase, replyTo) => ???
      case GetUser(email, replyTo)                    => ???
      case GetUserResponse(email, replyTo)            => ???
    }
}
