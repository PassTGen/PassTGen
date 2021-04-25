package passtgen.passgen

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import akka.actor.typed.ActorSystem
import akka.util.Timeout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.typed.ActorRef
import passtgen.JsonSupport

class PasswordGenRoutes(passgen: ActorRef[PasswordGen.Command])(implicit
    system: ActorSystem[_]
) extends JsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable
  import PasswordGen._

  implicit val timeout: Timeout = 3.seconds

  lazy val passgenRoutes: Route =
    pathPrefix("passgen") {
      concat(
        path("password") {
          parameters(
            "email",
            "length".as[Int] ? 10,
            "symbols" ? "alphanumeric",
            "capitalize".as[Boolean] ? true
          ) { (email, length, symbols, capitalize) =>
            {
              val genParameters: Seq[GeneratorParameters] = Seq(
                Length(length),
                symbols.toLowerCase match {
                  case "alphanumericspecial" => AlphaNumericSpecial
                  case "alpha"               => Alpha
                  case _                     => AlphaNumeric
                },
                Capitalize(capitalize)
              )
              val generatePassword: Future[Command] =
                passgen.ask(
                  Generate(email, "password", genParameters, _)
                )
              onSuccess(generatePassword) {
                case PasswordGeneratedOK(password) => complete(password)
                case AuthUserFailure(ex) =>
                  complete(StatusCodes.Unauthorized -> ex)
              }
            }
          }
        },
        path("passphrase") {
          parameters(
            "email",
            "length".as[Int] ? 4
          ) { (email, length) =>
            {
              val genParameters: Seq[GeneratorParameters] = Seq(
                Length(length)
              )
              val generatePassword: Future[Command] =
                passgen.ask(
                  Generate(email, "password", genParameters, _)
                )
              onSuccess(generatePassword) {
                case PasswordGeneratedOK(password) => complete(password)
                case AuthUserFailure(ex) =>
                  complete(StatusCodes.Unauthorized -> ex)
              }
            }
          }
        }
      )
    }
}
