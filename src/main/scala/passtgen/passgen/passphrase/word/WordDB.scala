package passtgen.passgen.passphrase.word

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.concurrent.ExecutionContext
import scala.util.Success
import passtgen.CommonDb
import reactivemongo.api.bson._
import reactivemongo.api.bson.collection.BSONCollection
object WordDB {

  def apply(implicit ctx: ExecutionContext): WordDB =
    new WordDB()
}
class WordDB(implicit val ctx: ExecutionContext) {
  import WordCodec._
  val wordList: Future[BSONCollection] =
    CommonDb(ctx).passtgendb.map(_.collection("wordlist"))
  def getWord(id: Int) = {
    wordList
      .flatMap(
        _.find(BSONDocument("id" -> BSONDocument("$eq" -> id)))
          .one[Word]
      )
      .transform(
        (s: Option[Word]) => {
          s match {
            case None        => throw new Exception("No se ha encontrado")
            case Some(value) => value.word
          }
        },
        (f: Throwable) => {
          ctx.reportFailure(f)
          f
        }
      )
  }
}
