package passtgen
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import passtgen.auth.user.User
import passtgen.passgen.PassGen._
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val userFormat = jsonFormat1(User(_))
  implicit val passwordFormat = jsonFormat1(PasswordGeneratedOK(_))
  implicit val passphraseFormat = jsonFormat1(PassphraseGeneratedOK(_))
}
