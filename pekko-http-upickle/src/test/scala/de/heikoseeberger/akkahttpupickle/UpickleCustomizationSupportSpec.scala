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

package de.heikoseeberger.akkahttpupickle

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import upickle.AttributeTagged
import upickle.core.Visitor

final class UpickleCustomizationSupportSpec
    extends AsyncWordSpec
    with Matchers
    with BeforeAndAfterAll {

  private implicit val system = ActorSystem()

  object FooApi extends AttributeTagged {
    override implicit val IntWriter: FooApi.Writer[Int] = new Writer[Int] {
      override def write0[V](out: Visitor[_, V], v: Int): V = out.visitString("foo", -1)
    }
  }
  object UpickleFoo extends UpickleCustomizationSupport {
    override type Api = FooApi.type
    override def api: FooApi.type = FooApi
  }

  import UpickleFoo._

  "UpickleCustomizationSupport" should {
    "support custom configuration" in {
      Marshal(123)
        .to[RequestEntity]
        .flatMap(Unmarshal(_).to[String])
        .map(_ shouldBe "foo")
    }
  }

  override protected def afterAll() = {
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}
