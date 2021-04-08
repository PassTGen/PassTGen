package passtgen.auth.user
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.bson.codecs.configuration.CodecRegistries.{
  fromRegistries,
  fromProviders
}
import org.bson.codecs.configuration.CodecRegistry
object UserCodec {
  def apply(): CodecRegistry =
    fromRegistries(fromProviders(classOf[User]), DEFAULT_CODEC_REGISTRY)
}
