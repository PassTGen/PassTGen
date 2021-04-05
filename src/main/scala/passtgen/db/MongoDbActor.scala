package passtgen.db

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.Behaviors

object MongoDbActor {
  def apply(groupId: String): Behavior[Command] =
    Behaviors.setup(context => new MongoDbActor(context))

  trait Command

}
class MongoDbActor(context: ActorContext[MongoDbActor.Command])
    extends AbstractBehavior[MongoDbActor.Command](context) {
  import DbManagerActor.{
    GetPassphrase,
    GetUser,
    CreateUser,
    GetPassphraseResponse,
    GetUserResponse
  }
  import MongoDbActor.Command

  override def onMessage(
      msg: MongoDbActor.Command
  ): Behavior[MongoDbActor.Command] = ???
}
