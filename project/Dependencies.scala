import sbt._

object Dependencies {

  object v {
    val tapir = "1.1.3"
  }

  lazy val rootDeps = Seq(
    "com.github.fd4s" %% "fs2-kafka" % "2.5.0",
    "com.softwaremill.sttp.tapir" %% "tapir-core" % v.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % v.tapir,
    "org.http4s" %% "http4s-blaze-server" % "0.23.12",
    "org.typelevel" %% "cats-effect" % "3.3.14",
    "co.fs2" %% "fs2-core" % "3.3.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
  )
}
