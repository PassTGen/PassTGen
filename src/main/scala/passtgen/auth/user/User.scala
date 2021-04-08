package passtgen.auth.user
import org.mongodb.scala.bson.ObjectId
case class User(val _id: ObjectId, val email: String)

object User {
  def apply(_id: ObjectId, email: String): Option[User] = {
    if (checkEmail(email)) Some(new User(_id, email))
    else None
  }
  def checkEmail(email: String): Boolean = {
    val emailRegex =
      "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}\\Z".r
    emailRegex.matches(email.toUpperCase)
  }
}
