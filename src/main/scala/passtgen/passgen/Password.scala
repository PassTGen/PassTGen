package passtgen.passgen
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import passtgen.passgen.Parameters
object Password {
  val defaultParameters: Seq[Parameters] = Seq(
    Length(10),
    Capitalize(true),
    AlphaNumeric
  )
  val minLength = Length(8)
  val maxLength = Length(64)
  def apply(): Password = new Password(defaultParameters)
  def apply(parameters: Parameters*): Password =
    new Password(
      sanitizeLength(parameters)
    )
  def sanitizeLength(parameters: Seq[Parameters]): Seq[Parameters] =
    parameters.map({
      case Length(n) => {
        if (n > maxLength.n) maxLength
        else if (n < minLength.n) minLength
        else Length(n)
      }
      case a: Parameters => a
    })
}

class Password(val parameters: Seq[Parameters]) {

  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  def generatePassword(): Future[String] = Future {
    val Length(length) = parameters.find(a => a.isInstanceOf[Length]) match {
      case Some(value) => value
      case None =>
        Password.defaultParameters.find(a => a.isInstanceOf[Length]).get
    }

    val Capitalize(capitalize) =
      parameters.find(a => a.isInstanceOf[Capitalize]) match {
        case Some(value) => value
        case None =>
          Password.defaultParameters.find(a => a.isInstanceOf[Capitalize]).get
      }

    val symbol = parameters.find(a => a.isInstanceOf[Symbols]) match {
      case Some(value) => value
      case None =>
        Password.defaultParameters.find(a => a.isInstanceOf[Symbols]).get
    }

    val positions = symbol match {
      case Alpha => {
        if (capitalize) {
          normalChars(length / 2) ::
            upperChars(length / 2)
        } else {
          normalChars(length)
        }
      }
      case AlphaNumeric => {
        if (capitalize) {
          normalChars(length / 3) ::
            upperChars(length / 3) ::
            numChars(length / 3)
        } else {
          normalChars(length / 2) ::
            numChars(length / 2)
        }
      }
      case AlphaNumericSpecial => {
        if (capitalize) {
          normalChars(length / 4) ::
            upperChars(length / 4) ::
            numChars(length / 4) ::
            specialChars(length / 4)
        } else {
          normalChars(length / 2) ::
            numChars(length / 4) ::
            specialChars(length / 4)
        }
      }
    }
    val filledpositions =
      if (positions.length < length)
        positions :: normalChars(length - positions.length)
      else positions
    filledpositions.mkString
  }

  def normalChars(n: Int): List[Char] = Range(0, n).map(_ => 'a').toList
  def numChars(n: Int): List[Char] = Range(0, n).map(_ => 'n').toList
  def upperChars(n: Int): List[Char] = Range(0, n).map(_ => 'u').toList
  def specialChars(n: Int): List[Char] = Range(0, n).map(_ => 's').toList
}
