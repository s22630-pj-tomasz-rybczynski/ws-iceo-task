package co.iceo

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp, Resource}
import co.iceo.algebra.Counter.LiveCounter
import co.iceo.api.IncrementRoute
import cats.syntax.all._
import co.iceo.config.AppConfig
import co.iceo.kafka.IntProducer
import org.http4s.blaze.server.BlazeServerBuilder


object IOMain extends IOApp.Simple {
  override def run: IO[Unit] =
    (for {
      ref <- Resource.eval(Ref.of[IO, Int](0))
      counter = new LiveCounter[IO](ref)
      cfg = AppConfig(brokerUrl = "0.0.0.0:9092", groupId = "counter", topic = "counter", serverUrl = "0.0.0.0", serverPort = 8088)
      producer = new IntProducer[IO](cfg.brokerUrl, cfg.groupId, cfg.topic)
      program = new Program[IO](counter, cfg)
      produceOne <- producer.makeProducer
      server = BlazeServerBuilder[IO]
        .bindHttp(cfg.serverPort, cfg.serverUrl)
        .withHttpWebSocketApp(IncrementRoute[IO](produceOne, counter, _).orNotFound)
    } yield (program, server)).use {
      case (program, server) => (program.run, server.serve.compile.drain).parTupled.void
    }
}
