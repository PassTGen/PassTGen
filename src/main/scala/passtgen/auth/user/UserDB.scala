package passtgen.auth.user

import scala.concurrent.Future
import scala.util.Failure
import scala.concurrent.ExecutionContext
import scala.util.Success
import passtgen.CommonDb
import reactivemongo.api.bson._
import reactivemongo.api.bson.collection.BSONCollection
import passtgen.auth.user.User

object UserDB {
  def apply(implicit ctx: ExecutionContext): UserDB =
    new UserDB()

}
class UserDB(implicit val ctx: ExecutionContext) {
  import UserCodec._
  val userCollection: Future[BSONCollection] =
    CommonDb(ctx).map(_.collection("users"))

  def createUser(email: String): Future[User] =
    User(email) match {
      case null => Future.failed[User](new Exception("IncorrectEmailFormat"))
      case user: User =>
        userCollection.flatMap(_.insert.one(user).map(_ => user))
    }

  def getUser(email: String): Future[Option[User]] = {
    userCollection
      .flatMap(
        _.find(BSONDocument("email" -> BSONDocument("$eq" -> email)))
          .one[User]
      )
  }
}
