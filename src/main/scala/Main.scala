import user.User
object Main {
  def main(args: Array[String]): Unit = {
    println("principal")
    val us = User("pedro", "ppp@ppp2p.com").get
    val us2 = User("pedro", "ppp@pppp.com").get
    println(us equals us2)
  }
}
