package passtgen.auth.user

object User {
  final case class User(val email: String)
  def apply(email: String): User = {
    if (checkEmail(email)) new User(email)
    else null
  }
  def checkEmail(email: String): Boolean = {
    val emailRegex =
      "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}\\Z".r
    emailRegex.matches(email.toUpperCase)
  }
}
