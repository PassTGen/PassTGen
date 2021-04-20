package passtgen.passgen.passphrase

import scala.concurrent.ExecutionContext
import passtgen.passgen.Length
import scala.concurrent.Future
import scala.util.Random
import passtgen.passgen.passphrase.word._
import scala.util.Failure
import scala.util.Success

// https://www.eff.org/dice
// https://www.eff.org/files/2016/09/08/eff_short_wordlist_2_0.txt

object Passphrase {
  val defaultLength = Length(4)
  def apply() =
    new Passphrase(defaultLength)
  def apply(length: Length) =
    new Passphrase(length)

  def generateRandomIndex: Int = {
    Range(0, 4).map(_ => Random.between(1, 7)).fold(0)((x, y) => x * 10 + y)
  }
  def generateRandomSeparator: String = {
    Random.shuffle("-_.!|,+=&@#%").head.toString
  }
}
class Passphrase(val length: Length) {
  import Passphrase._
  def generatePassphrase(implicit ctx: ExecutionContext): Future[String] =
    Future {
      val db = WordDB(ctx)
      Range(0, length.n)
        .map(_ =>
          db.getWord(generateRandomIndex)
            .map({
              case None        => throw new Exception("Didn't Connect to DB")
              case Some(value) => value
            })
        )
        .mkString(generateRandomSeparator)
    }
}
