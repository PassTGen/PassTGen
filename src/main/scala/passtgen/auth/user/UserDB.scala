package passtgen.auth.user

import scala.concurrent.Future
import scala.util.Failure
import scala.concurrent.ExecutionContext
import scala.util.Success
import passtgen.CommonDb
import reactivemongo.api.bson._
import reactivemongo.api.bson.collection.BSONCollection
import User._

object UserDB {
  def apply(implicit ctx: ExecutionContext): UserDB =
    new UserDB(CommonDb(ctx), ctx)

}
class UserDB(val commondb: CommonDb, implicit val ctx: ExecutionContext) {
  import UserCodec._
  val userCollection: Future[BSONCollection] =
    commondb.passtgendb.map(_.collection("users"))

  def createUser(email: String): Future[User] =
    User(email) match {
      case user: User =>
        userCollection.flatMap(_.insert.one(user).map(_ => user))
      case _ => Future.failed[User](new Exception("IncorrectEmailFormat"))
    }

  def getUser(email: String): Future[Option[User]] = {
    userCollection
      .flatMap(
        _.find(BSONDocument("email" -> BSONDocument("$eq" -> email)))
          .one[User]
      )
  }
}
