package passtgen.passgen.passphrase.word

import scala.concurrent.Future
import scala.util.Failure
import scala.concurrent.ExecutionContext
import scala.util.Success
import passtgen.CommonDb
import reactivemongo.api.bson._
import reactivemongo.api.bson.collection.BSONCollection
object WordDB {

  def apply(ctx: ExecutionContext): WordDB =
    new WordDB(CommonDb(ctx), ctx)
}
class WordDB(val commondb: CommonDb, implicit val ctx: ExecutionContext) {
  import WordCodec._
  val wordList: Future[BSONCollection] =
    commondb.passtgendb.map(_.collection("wordlist"))
  def getWord(id: Int): Future[Option[Word]] = {
    wordList
      .flatMap(
        _.find(BSONDocument("id" -> BSONDocument("$eq" -> id)))
          .one[Word]
      )

  }
}
