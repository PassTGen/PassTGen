package passtgen.auth.user

import org.mongodb.scala._

import scala.concurrent.Future

object DbManager {
  def CreateUser(email: String): Future[User] = ???
  def GetUser(email: String): Future[User] = ???
}
