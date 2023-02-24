/*
 * Copyright 2015 Heiko Seeberger
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

package com.github.pjfanning.pekkohttpcirce

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.{ Sink, Source }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.{ BeforeAndAfterAll, EitherValues }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final class CirceSupportSpec2
    extends AsyncWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with EitherValues {

  import CirceSupportSpec._

  private implicit val system: ActorSystem = ActorSystem()

  /**
    * Specs common to both [[FailFastCirceSupport]] and [[ErrorAccumulatingCirceSupport]]
    */
  private def commonCirceSupport(support: BaseCirceSupport) = {
    import io.circe.generic.auto._
    import support._

    "enable streamed marshalling and unmarshalling for json arrays" in {
      val foos = (0 to 100).map(i => Foo(s"bar-$i")).toList

      Marshal(Source(foos))
        .to[ResponseEntity]
        .flatMap(entity => Unmarshal(entity).to[SourceOf[Foo]])
        .flatMap(_.runWith(Sink.seq))
        .map(_ shouldBe foos)
    }

  }

  "FailFastCirceSupport" should {
    behave like commonCirceSupport(FailFastCirceSupport)
  }

  "ErrorAccumulatingCirceSupport" should {
    behave like commonCirceSupport(ErrorAccumulatingCirceSupport)

  }

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}
