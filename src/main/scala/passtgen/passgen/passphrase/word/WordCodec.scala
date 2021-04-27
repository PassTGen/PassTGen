package passtgen.passgen.passphrase.word
import reactivemongo.api.bson.BSONDocumentWriter
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.BSONDocumentReader
object WordCodec {
  implicit def wordReader: BSONDocumentReader[Word] = Macros.reader[Word]
  implicit def wordWriter: BSONDocumentWriter[Word] = Macros.writer[Word]
}
