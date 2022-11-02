package co.iceo.algebra

import cats.Functor
import cats.effect.kernel.Ref

trait Counter[F[_]] {
  def increment(value: Int): F[Unit]
  def get: F[Int]
}

object Counter {
  final class LiveCounter[F[_]: Functor](ref: Ref[F, Int]) extends Counter[F] {
    override def increment(value: Int): F[Unit] = ref.update(_ + value)
    override def get: F[Int] = ref.get
  }
}
