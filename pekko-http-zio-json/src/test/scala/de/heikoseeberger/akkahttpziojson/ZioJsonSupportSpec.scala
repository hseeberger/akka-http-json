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

package de.heikoseeberger.akkahttpziojson

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model.{ HttpEntity, RequestEntity, ResponseEntity }
import org.apache.pekko.http.scaladsl.model.ContentTypes.{ `application/json`, `text/plain(UTF-8)` }
import org.apache.pekko.http.scaladsl.unmarshalling.{ Unmarshal, Unmarshaller }
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import org.apache.pekko.stream.scaladsl.{ Sink, Source }
import org.scalatest.{ BeforeAndAfterAll, EitherValues }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import zio.json._

object ZioJsonSupportSpec {

  final case class Foo(bar: String) {
    require(bar startsWith "bar", "bar must start with 'bar'!")
  }

  final case class MultiFoo(a: String, b: String)

  final case class OptionFoo(a: Option[String])

  implicit val fooEncoder: JsonEncoder[Foo]             = DeriveJsonEncoder.gen
  implicit val multiFooEncoder: JsonEncoder[MultiFoo]   = DeriveJsonEncoder.gen
  implicit val optionFooEncoder: JsonEncoder[OptionFoo] = DeriveJsonEncoder.gen

  implicit val fooDecoder: JsonDecoder[Foo]             = DeriveJsonDecoder.gen
  implicit val multiFooDecoder: JsonDecoder[MultiFoo]   = DeriveJsonDecoder.gen
  implicit val optionFooDecoder: JsonDecoder[OptionFoo] = DeriveJsonDecoder.gen

  implicit val rt: zio.Runtime[Any] = zio.Runtime.default
}

final class ZioJsonSupportSpec
    extends AsyncWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with EitherValues {
  import ZioJsonSupportSpec._

  private implicit val system: ActorSystem = ActorSystem()

  "ZioJsonSupport" should {
    import ZioJsonSupport._

    "enable marshalling and unmarshalling objects for generic derivation" in {
      val foo = Foo("bar")
      Marshal(foo)
        .to[RequestEntity]
        .flatMap(Unmarshal(_).to[Foo])
        .map(_ shouldBe foo)
    }

    "enable streamed marshalling and unmarshalling for json arrays" in {
      val foos = (0 to 100).map(i => Foo(s"bar-$i")).toList

      Marshal(Source(foos))
        .to[ResponseEntity]
        .flatMap(entity => Unmarshal(entity).to[SourceOf[Foo]])
        .flatMap(_.runWith(Sink.seq))
        .map(_ shouldBe foos)
    }

    "provide proper error messages for requirement errors" in {
      val entity = HttpEntity(`application/json`, """{ "bar": "baz" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(_ should have message "requirement failed: bar must start with 'bar'!")
    }

    "fail with NoContentException when unmarshalling empty entities" in {
      val entity = HttpEntity.empty(`application/json`)
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(_ shouldBe Unmarshaller.NoContentException)
    }

    "fail with UnsupportedContentTypeException when Content-Type is not `application/json`" in {
      val entity = HttpEntity("""{ "bar": "bar" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(
          _ shouldBe UnsupportedContentTypeException(Some(`text/plain(UTF-8)`), `application/json`)
        )
    }

    "not write None" in {
      val optionFoo = OptionFoo(None)
      Marshal(optionFoo)
        .to[RequestEntity]
        .map(_.asInstanceOf[HttpEntity.Strict].data.decodeString("UTF-8") shouldBe "{}")
    }

    "fail when unmarshalling empty entities with safeUnmarshaller" in {
      val entity = HttpEntity.empty(`application/json`)
      Unmarshal(entity)
        .to[Either[String, Foo]]
        .futureValue
        .left
        .value shouldBe a[String]
    }

    val errorMessage = """.a(expected '"' got '1')"""

    "fail-fast and return only the first unmarshalling error" in {
      val entity = HttpEntity(`application/json`, """{ "a": 1, "b": 2 }""")
      Unmarshal(entity)
        .to[MultiFoo]
        .failed
        .map(_.getMessage())
        .map(_ shouldBe errorMessage)
    }

    "fail-fast and return only the first unmarshalling error with safeUnmarshaller" in {
      val entity = HttpEntity(`application/json`, """{ "a": 1, "b": 2 }""")
      Unmarshal(entity)
        .to[Either[String, MultiFoo]]
        .futureValue
        .left
        .value shouldBe errorMessage
    }

    "allow unmarshalling with passed in Content-Types" in {
      val foo = Foo("bar")

      val entity = HttpEntity(`application/json`, """{ "bar": "bar" }""")
      Unmarshal(entity).to[Foo].map(_ shouldBe foo)
    }
  }

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}
