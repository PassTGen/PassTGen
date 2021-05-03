package passtgen
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import passtgen.auth.user.User
import passtgen.passgen.PasswordGen.PasswordGeneratedOK
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val userFormat = jsonFormat1(User.apply)
  implicit val passwordFormat = jsonFormat1(PasswordGeneratedOK)
}
