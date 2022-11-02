package co.iceo.kafka

import cats.effect.kernel.{Async, Resource}
import fs2.kafka.{KafkaProducer, ProducerSettings}
import cats.syntax.all._

import java.util.UUID

final class IntProducer[F[_]: Async](brokerUrl: String, groupId: String, topic: String) {
  private val producerSettings: ProducerSettings[F, String, Option[Int]] =
    ProducerSettings[F, String, Option[Int]]
      .withBootstrapServers(brokerUrl)

  def makeProducer: Resource[F, (UUID, Option[Int]) => F[Unit]] =
    KafkaProducer
      .resource(producerSettings)
      .map { producer => (key: UUID, value: Option[Int]) =>
        producer.produceOne_(topic, key.toString, value).flatten.void
      }
}
