/*
 * Copyright 2020-2022 Typelevel
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

package cats.effect
package laws

import cats.Applicative
import cats.effect.kernel.Clock
import cats.syntax.all._

trait ClockLaws[F[_]] {

  implicit val F: Clock[F]
  private implicit def app: Applicative[F] = F.applicative

  def monotonicity = (F.monotonic, F.monotonic).mapN(_ <= _)
}

object ClockLaws {
  def apply[F[_]](implicit F0: Clock[F]): ClockLaws[F] =
    new ClockLaws[F] { val F = F0 }
}
