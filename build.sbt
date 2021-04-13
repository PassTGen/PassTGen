lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "xyz.javiertejedor",
      scalaVersion := "2.13.5"
    )
  ),
  name := "passtgen"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)

libraryDependencies ++= Seq("org.reactivemongo" %% "reactivemongo" % "1.0.3")
