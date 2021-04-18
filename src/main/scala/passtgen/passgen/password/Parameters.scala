package passtgen.passgen

sealed trait Parameters
case class Length(n: Int) extends Parameters
case class Capitalize(n: Boolean) extends Parameters

sealed trait Symbols extends Parameters
case object Alpha extends Symbols
case object AlphaNumeric extends Symbols
case object AlphaNumericSpecial extends Symbols
