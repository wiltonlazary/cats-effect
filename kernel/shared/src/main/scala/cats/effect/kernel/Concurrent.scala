/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect.kernel

import cats.{Monoid, Semigroup}
import cats.data.{EitherT, IorT, Kleisli, OptionT, WriterT}

trait Concurrent[F[_], E] extends Spawn[F, E] {

  def ref[A](a: A): F[Ref[F, A]]

  def deferred[A]: F[Deferred[F, A]]

}

object Concurrent {
  def apply[F[_], E](implicit F: Concurrent[F, E]): F.type = F
  def apply[F[_]](implicit F: Concurrent[F, _], d: DummyImplicit): F.type = F

  implicit def concurrentForOptionT[F[_], E](
      implicit F0: Concurrent[F, E]): Concurrent[OptionT[F, *], E] =
    new OptionTConcurrent[F, E] {
      override implicit protected def F: Concurrent[F, E] = F0
    }

  implicit def concurrentForEitherT[F[_], E0, E](
      implicit F0: Concurrent[F, E]): Concurrent[EitherT[F, E0, *], E] =
    new EitherTConcurrent[F, E0, E] {
      override implicit protected def F: Concurrent[F, E] = F0
    }

  implicit def concurrentForKleisli[F[_], R, E](
      implicit F0: Concurrent[F, E]): Concurrent[Kleisli[F, R, *], E] =
    new KleisliConcurrent[F, R, E] {
      override implicit protected def F: Concurrent[F, E] = F0
    }

  implicit def concurrentForIorT[F[_], L, E](
      implicit F0: Concurrent[F, E],
      L0: Semigroup[L]): Concurrent[IorT[F, L, *], E] =
    new IorTConcurrent[F, L, E] {
      override implicit protected def F: Concurrent[F, E] = F0

      override implicit protected def L: Semigroup[L] = L0
    }

  implicit def concurrentForWriterT[F[_], L, E](
      implicit F0: Concurrent[F, E],
      L0: Monoid[L]): Concurrent[WriterT[F, L, *], E] =
    new WriterTConcurrent[F, L, E] {
      override implicit protected def F: Concurrent[F, E] = F0

      override implicit protected def L: Monoid[L] = L0
    }

  private[kernel] trait OptionTConcurrent[F[_], E]
      extends Concurrent[OptionT[F, *], E]
      with Spawn.OptionTSpawn[F, E] {
    implicit protected def F: Concurrent[F, E]

    override def ref[A](a: A): OptionT[F, Ref[OptionT[F, *], A]] =
      OptionT.liftF(F.map(F.ref(a))(_.mapK(OptionT.liftK)))

    override def deferred[A]: OptionT[F, Deferred[OptionT[F, *], A]] =
      OptionT.liftF(F.map(F.deferred[A])(_.mapK(OptionT.liftK)))
  }

  private[kernel] trait EitherTConcurrent[F[_], E0, E]
      extends Concurrent[EitherT[F, E0, *], E]
      with Spawn.EitherTSpawn[F, E0, E] {
    implicit protected def F: Concurrent[F, E]

    override def ref[A](a: A): EitherT[F, E0, Ref[EitherT[F, E0, *], A]] =
      EitherT.liftF(F.map(F.ref(a))(_.mapK(EitherT.liftK)))

    override def deferred[A]: EitherT[F, E0, Deferred[EitherT[F, E0, *], A]] =
      EitherT.liftF(F.map(F.deferred[A])(_.mapK(EitherT.liftK)))
  }

  private[kernel] trait KleisliConcurrent[F[_], R, E]
      extends Concurrent[Kleisli[F, R, *], E]
      with Spawn.KleisliSpawn[F, R, E] {
    implicit protected def F: Concurrent[F, E]

    override def ref[A](a: A): Kleisli[F, R, Ref[Kleisli[F, R, *], A]] =
      Kleisli.liftF(F.map(F.ref(a))(_.mapK(Kleisli.liftK)))

    override def deferred[A]: Kleisli[F, R, Deferred[Kleisli[F, R, *], A]] =
      Kleisli.liftF(F.map(F.deferred[A])(_.mapK(Kleisli.liftK)))
  }

  private[kernel] trait IorTConcurrent[F[_], L, E]
      extends Concurrent[IorT[F, L, *], E]
      with Spawn.IorTSpawn[F, L, E] {
    implicit protected def F: Concurrent[F, E]

    implicit protected def L: Semigroup[L]

    override def ref[A](a: A): IorT[F, L, Ref[IorT[F, L, *], A]] =
      IorT.liftF(F.map(F.ref(a))(_.mapK(IorT.liftK)))

    override def deferred[A]: IorT[F, L, Deferred[IorT[F, L, *], A]] =
      IorT.liftF(F.map(F.deferred[A])(_.mapK(IorT.liftK)))
  }

  private[kernel] trait WriterTConcurrent[F[_], L, E]
      extends Concurrent[WriterT[F, L, *], E]
      with Spawn.WriterTSpawn[F, L, E] {

    implicit protected def F: Concurrent[F, E]

    implicit protected def L: Monoid[L]

    override def ref[A](a: A): WriterT[F, L, Ref[WriterT[F, L, *], A]] =
      WriterT.liftF(F.map(F.ref(a))(_.mapK(WriterT.liftK)))

    override def deferred[A]: WriterT[F, L, Deferred[WriterT[F, L, *], A]] =
      WriterT.liftF(F.map(F.deferred[A])(_.mapK(WriterT.liftK)))
  }

}