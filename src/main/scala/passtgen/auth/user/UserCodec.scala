package passtgen.auth.user

import reactivemongo.api.bson.BSONDocumentWriter
import reactivemongo.api.bson.Macros
import reactivemongo.api.bson.BSONDocumentReader
import passtgen.auth.user.User
object UserCodec {
  implicit def userWriter: BSONDocumentWriter[User] = Macros.writer[User]
  implicit def userReader: BSONDocumentReader[User] = Macros.reader[User]
}
