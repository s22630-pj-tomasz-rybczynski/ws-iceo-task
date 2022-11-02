package co.iceo.api

import cats.effect.kernel.Async
import cats.syntax.all._
import co.iceo.algebra.Counter
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s._
import fs2._

import scala.concurrent.duration._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._

import java.util.UUID

object IncrementRoute {

  def apply[F[_]: Async](produceOne: (UUID, Option[Int]) => F[Unit], counter: Counter[F], ws: WebSocketBuilder2[F]): HttpRoutes[F] = {

    val incrementEndpoint: PublicEndpoint[Unit, Unit, Pipe[F, String, String], Fs2Streams[F] with WebSockets] =
      endpoint.in("ws").out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[F]))

    def handler: F[Pipe[F, String, String]] = Async[F].delay {
      _.evalMap { input =>
        for {
          key <- Async[F].delay(UUID.randomUUID)
          initial <- counter.get
          _ <- produceOne(key, input.toIntOption)
          value <- Async[F].timeoutTo(counter.get.iterateUntil(_ != initial), 250.millis, Async[F].delay(initial)) // I don't like this, handles if input == 0
          res <- Async[F].delay(value.toString)
        } yield res
      }
    }

    Http4sServerInterpreter[F]().toWebSocketRoutes(incrementEndpoint.serverLogicSuccess(_ => handler))(ws)
  }
}
