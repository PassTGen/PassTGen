package passtgen.passgen

sealed trait GeneratorParameters
case class Length(n: Int) extends GeneratorParameters
case class Capitalize(n: Boolean) extends GeneratorParameters

sealed trait Symbols extends GeneratorParameters
case object Alpha extends Symbols
case object AlphaNumeric extends Symbols
case object AlphaNumericSpecial extends Symbols
