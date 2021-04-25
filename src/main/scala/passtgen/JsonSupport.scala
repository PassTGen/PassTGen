package passtgen
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import passtgen.auth.user.User
import passtgen.passgen.PasswordGen
trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val userFormat = jsonFormat1(User.User)
  implicit val passwordFormat = jsonFormat1(PasswordGen.PasswordGeneratedOK)
}
