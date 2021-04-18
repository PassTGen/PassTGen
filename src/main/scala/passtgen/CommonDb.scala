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
object CommonDb {

  implicit val driver = AsyncDriver()
  def apply(implicit ctx: ExecutionContext) = new CommonDb
}

class CommonDb(
    implicit val ctx: ExecutionContext,
    implicit val driver: AsyncDriver
) {
  val mongoUri = ""
  val parsedUri = MongoConnection.fromString(mongoUri)
  val futureConnection = parsedUri.flatMap(driver.connect(_))
  def passtgendb: Future[DB] =
    futureConnection.flatMap(_.database("passtgendb"))
}
