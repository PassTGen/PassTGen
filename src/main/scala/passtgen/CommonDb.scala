package passtgen
import reactivemongo.api.{Cursor, DB, MongoConnection, AsyncDriver}
import reactivemongo.api.bson.{
  BSONDocumentWriter,
  BSONDocumentReader,
  Macros,
  document
}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import reactivemongo.api.MongoConnectionOptions
import com.typesafe.config.ConfigFactory
object CommonDb {
  implicit val driver = AsyncDriver()
  val config = ConfigFactory.load()
  val host = config.getString("mongo.host")
  val port = config.getInt("mongo.port")
  val dbName = config.getString("mongo.dbName")
  val userName = config.getString("mongo.user")
  val password = config.getString("mongo.password")
  val connectionOptions =
    MongoConnectionOptions(
      authenticationDatabase = Some(dbName),
      credentials = Map(
        dbName -> MongoConnectionOptions.Credential(userName, Some(password))
      )
    )
  val connection: Future[MongoConnection] =
    driver.connect(
      List(s"$host:$port/$dbName"),
      connectionOptions
    )
  def apply(implicit ctx: ExecutionContext) = {
    connection
      .flatMap(_.database(dbName))
  }
}
