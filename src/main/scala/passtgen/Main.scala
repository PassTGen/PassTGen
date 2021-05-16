package passtgen
import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val host = config.getString("server.host")
    val port = config.getInt("server.port")
    val system: ActorSystem[HttpServer.Message] =
      ActorSystem(HttpServer(host, port), "server")
  }
}
