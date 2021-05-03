package passtgen.auth.user

object User {

  def apply(email: String): User = {
    if (checkEmail(email)) new User(email)
    else null
  }
  def checkEmail(email: String): Boolean = {
    val emailRegex =
      "[^@ \t\r\n]+@[^@ \t\r\n]+\\.[^@ \t\r\n]+".r
    emailRegex.matches(email)
  }

}

final case class User(val email: String)
