package passtgen
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config._
import passtgen.auth.Registration
import passtgen.auth.RegistrationRoute
import passtgen.passgen.PassGen
import passtgen.passgen.PasswordGenRoutes

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object HttpServer {

  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message
  def apply(host: String, port: Int): Behavior[Message] =
    Behaviors.setup { ctx =>
      implicit val system = ctx.system

      val buildRegistration = ctx.spawn(Registration(), "Registration")
      val registrationRoute = new RegistrationRoute(buildRegistration)
      val buildPassGenerator = ctx.spawn(PassGen(), "PassGen")
      val passwordRoutes = new PasswordGenRoutes(buildPassGenerator)

      lazy val topLevel: Route =
        concat(
          pathPrefix("register")(registrationRoute.registrationRoute),
          pathPrefix("passgen")(passwordRoutes.passgenRoutes)
        )

      val serverBinding: Future[Http.ServerBinding] =
        Http().newServerAt(host, port).bind(topLevel)
      ctx.pipeToSelf(serverBinding) {
        case Success(binding) => Started(binding)
        case Failure(ex)      => StartFailed(ex)
      }

      def running(binding: ServerBinding): Behavior[Message] =
        Behaviors
          .receiveMessagePartial[Message] { case Stop =>
            ctx.log.info(
              "Stopping server http://{}:{}/",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            Behaviors.stopped
          }
          .receiveSignal { case (_, PostStop) =>
            binding.unbind()
            Behaviors.same
          }

      def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
        Behaviors.receiveMessage[Message] {
          case StartFailed(cause) =>
            throw new RuntimeException("Server failed to start", cause)
          case Started(binding) =>
            ctx.log.info(
              "Server online at http://{}:{}/",
              binding.localAddress.getHostString,
              binding.localAddress.getPort
            )
            if (wasStopped) ctx.self ! Stop
            running(binding)
          case Stop =>
            starting(wasStopped = true)
        }

      starting(wasStopped = false)
    }
}
