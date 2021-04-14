package passtgen.passgen.passphrase.word
case class Word(val id: Int, val word: String)

object Word {
  def apply(id: Int, word: String): Word =
    new Word(id, word)
}
