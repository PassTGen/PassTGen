package passtgen.auth

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import passtgen.auth.user.User._
import akka.actor.typed.ActorSystem
import akka.util.Timeout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.typed.ActorRef

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val userFormat = jsonFormat1(User)
}

class RegistrationRoute(authenticator: ActorRef[Registration.Command])(implicit
    system: ActorSystem[_]
) extends JsonSupport {

  import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
  import akka.actor.typed.scaladsl.AskPattern.Askable
  import Registration._

  implicit val timeout: Timeout = 3.seconds

  lazy val theJobRoutes: Route =
    pathPrefix("register") {
      concat(
        pathEnd {
          concat(
            post {
              entity(as[String]) { email =>
                val operationPerformed: Future[Command] =
                  authenticator.ask(Registration.CreateUser(email, _))
                onSuccess(operationPerformed) {
                  case OK(user) => complete(user)
                  case DatabaseFailure(reason) =>
                    complete(StatusCodes.InternalServerError -> reason)
                }
              }
            }
          )
        }
      )
    }
}
