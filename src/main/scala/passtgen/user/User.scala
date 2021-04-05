package user

class User(val name: String, val email: String) {
  override def toString(): String = {
    s"""|{
        |   "name": "${name}",
        |   "email": "${email}"
        |}""".stripMargin
  }
  def equals(x: User): Boolean = {
    x.email == this.email
  }
}

object User {
  def apply(name: String, email: String): Option[User] = {
    if (checkEmail(email)) Some(new User(name, email))
    else None
  }
  def checkEmail(email: String): Boolean = {
    val emailRegex =
      "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}\\Z".r
    emailRegex.matches(email.toUpperCase)
  }
}
