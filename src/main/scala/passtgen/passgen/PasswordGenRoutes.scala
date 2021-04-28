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

  implicit val timeout: Timeout = 120.seconds

  lazy val passgenRoutes: Route =
    concat(
      path("password") {
        parameters(
          "auth",
          "length".as[Int] ? 10,
          "symbols" ? "alphanumeric",
          "capitalize".as[Boolean] ? true
        ) { (auth, length, symbols, capitalize) =>
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
                Generate(auth, "password", genParameters, _)
              )
            onSuccess(generatePassword) {
              case PasswordGeneratedOK(password) =>
                complete(StatusCodes.OK -> password)
              case AuthUserFailure(ex) =>
                complete(StatusCodes.Unauthorized -> ex + "sdfasdf")
            }
          }
        }
      },
      path("passphrase") {
        parameters(
          "auth",
          "length".as[Int] ? 4
        ) { (auth, length) =>
          {
            val genParameters: Seq[GeneratorParameters] = Seq(
              Length(length)
            )
            val generatePassphrase: Future[Command] =
              passgen.ask(
                Generate(auth, "passphrase", genParameters, _)
              )
            onSuccess(generatePassphrase) {
              case PasswordGeneratedOK(password) =>
                complete(StatusCodes.OK -> password)
              case AuthUserFailure(ex) =>
                complete(StatusCodes.Unauthorized -> ex)
              case DatabaseFailure(ex) =>
                complete(StatusCodes.InternalServerError -> ex)
            }
          }
        }
      }
    )

}
