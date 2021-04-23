package passtgen.passgen
import scala.concurrent.Future
import passtgen.passgen.Parameters
import scala.util.Random
import scala.concurrent.ExecutionContext
// I took the base algortithm and modded it from here:
// https://github.com/bitwarden/jslib/blob/master/src/services/passwordGeneration.service.ts
object Password {

  val defaultParameters: Seq[Parameters] = Seq(
    Length(10),
    Capitalize(true),
    AlphaNumeric
  )

  val minLength = Length(8)
  val maxLength = Length(64)

  val lowerCharset: String = "abcdefghijklmnopqrstuvwxyz"
  val upperCharset: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  val numericCharset: String = "0123456789"
  val specialCharset: String = "!@#$%^&*()[]{}"

  def apply(parameters: Parameters*): Password =
    if (parameters != Nil)
      new Password(sanitizeLength(parameters))
    else
      new Password(defaultParameters)

  def sanitizeLength(parameters: Seq[Parameters]): Seq[Parameters] =
    parameters.map({
      case Length(n) => {
        if (n > maxLength.n) maxLength
        else if (n < minLength.n) minLength
        else Length(n)
      }
      case a: Parameters => a
    })

  def lowercasePositionChars(n: Int): List[Char] =
    if (n >= 0) Range(0, n).map(_ => 'l').toList else List[Char]()

  def uppercasePositionChars(n: Int, capitalize: Boolean): List[Char] =
    if (capitalize) Range(0, n).map(_ => 'u').toList else List[Char]()

  def numPositionChars(n: Int): List[Char] =
    Range(0, n).map(_ => 'n').toList

  def specialPositionChars(n: Int): List[Char] =
    Range(0, n).map(_ => 's').toList
}

class Password(val parameters: Seq[Parameters]) {
  import Password._

  private val Length(length) =
    parameters.find(a => a.isInstanceOf[Length]) match {
      case Some(value) => value
      case None =>
        defaultParameters.find(a => a.isInstanceOf[Length]).get
    }

  private val Capitalize(capitalize) =
    parameters.find(a => a.isInstanceOf[Capitalize]) match {
      case Some(value) => value
      case None =>
        defaultParameters.find(a => a.isInstanceOf[Capitalize]).get
    }

  private val symbol = parameters.find(a => a.isInstanceOf[Symbols]) match {
    case Some(value) => value.asInstanceOf[Symbols]
    case None =>
      defaultParameters
        .find(a => a.isInstanceOf[Symbols])
        .get
        .asInstanceOf[Symbols]
  }

  private val positions: List[Char] = symbol match {
    case Alpha => {
      lowercasePositionChars(length / 2) ++ uppercasePositionChars(
        length / 2,
        capitalize
      )
    }
    case AlphaNumeric => {
      lowercasePositionChars(length / 3) ++
        uppercasePositionChars(
          length / 3,
          capitalize
        ) ++
        numPositionChars(length / 3)
    }
    case AlphaNumericSpecial => {
      lowercasePositionChars(length / 4) ++
        uppercasePositionChars(
          length / 4,
          capitalize
        ) ++
        numPositionChars(length / 4) ++
        specialPositionChars(length / 4)
    }
  }

  def generatePassword(): Option[String] = Option {
    val filledPositions: List[Char] =
      positions ++ lowercasePositionChars(length - positions.length)
    Random
      .shuffle(filledPositions)
      .map({
        case 'l' => Random.shuffle(lowerCharset).head
        case 'u' => Random.shuffle(upperCharset).head
        case 'n' => Random.shuffle(numericCharset).head
        case 's' => Random.shuffle(specialCharset).head
      })
      .mkString
  }

}
