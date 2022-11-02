package co.iceo

import cats.effect.kernel.Async
import co.iceo.algebra.Counter
import co.iceo.config.AppConfig
import co.iceo.kafka.IntConsumer
import co.iceo.kafka.model.KafkaValue
import fs2.Pipe

final class Program[F[_]: Async](counter: Counter[F], config: AppConfig) extends IntConsumer[F](config.brokerUrl, config.groupId, config.topic) {
  override def handle: Pipe[F, KafkaValue[Int], Unit] = _.evalMap(kv => counter.increment(kv.value))
}
