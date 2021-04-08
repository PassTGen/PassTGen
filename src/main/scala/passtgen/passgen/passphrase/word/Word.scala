package passtgen.passgen.passphrase.word
import org.mongodb.scala.bson.ObjectId
case class Word(val _id: ObjectId, val id: Int, val word: String)

object Word {
  def apply(_id: ObjectId, id: Int, word: String): Word =
    new Word(_id, id, word)
}
