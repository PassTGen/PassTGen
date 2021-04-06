package user

case class User(val oid: String, val email: String) {
  override def toString(): String = {
    s"""|{
        |   "oid": "${oid}",
        |   "email": "${email}"
        |}""".stripMargin
  }
}

object User {
  def apply(oid: String, email: String): Option[User] = {
    if (checkEmail(email)) Some(new User(oid, email))
    else None
  }
  def checkEmail(email: String): Boolean = {
    val emailRegex =
      "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[A-Z0-9-]+\\.)+[A-Z]{2,6}\\Z".r
    emailRegex.matches(email.toUpperCase)
  }
}
