package passtgen.passgen.passphrase.word
import reactivemongo.api.bson.BSONDocumentWriter
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.BSONDocumentReader
object WordCodec {
  implicit def wordReader: BSONDocumentReader[Word] = Macros.reader[Word]
}
