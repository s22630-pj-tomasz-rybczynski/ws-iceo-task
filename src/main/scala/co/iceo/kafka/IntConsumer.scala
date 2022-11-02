package co.iceo.kafka

import cats.effect.kernel.Async
import co.iceo.kafka.model.KafkaValue
import fs2._
import fs2.kafka._

abstract class IntConsumer[F[_]: Async](brokerUrl: String, groupId: String, topic: String) {
  private val consumerSettings: ConsumerSettings[F, String, Option[Int]] =
    ConsumerSettings[F, String, Option[Int]]
      .withIsolationLevel(IsolationLevel.ReadCommitted)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(brokerUrl)
      .withGroupId(groupId)

  lazy val consumerPipeline: Stream[F, Unit] = {
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic)
      .flatMap(_.stream)
      .map(ccr => KafkaValue(ccr.record.value))
      .collect {
        case KafkaValue(Some(value)) => KafkaValue(value)
      }
      .through(handle)
  }

  def handle: Pipe[F, KafkaValue[Int], Unit]

  def run: F[Unit] = consumerPipeline.compile.drain
}
