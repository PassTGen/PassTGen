package passtgen.passgen.passphrase.word
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromRegistries,
  fromProviders
}
import org.bson.codecs.configuration.CodecRegistry
object WordCodec {
  def apply(): CodecRegistry =
    fromRegistries(fromProviders(classOf[Word]), DEFAULT_CODEC_REGISTRY)
}
