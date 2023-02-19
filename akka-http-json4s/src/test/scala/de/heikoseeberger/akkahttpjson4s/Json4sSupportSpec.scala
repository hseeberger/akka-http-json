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

package de.heikoseeberger.akkahttpjson4s

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.model.ContentTypes.{ `application/json`, `text/plain(UTF-8)` }
import org.apache.pekko.http.scaladsl.unmarshalling.{ Unmarshal, Unmarshaller }
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import org.apache.pekko.stream.scaladsl.{ Sink, Source }
import org.json4s.jackson.Serialization
import org.json4s.{ DefaultFormats, jackson, native }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Json4sSupportSpec {

  final case class Foo(bar: String) {
    require(bar startsWith "bar", "bar must start with 'bar'!")
  }
}

final class Json4sSupportSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  import Json4sSupport._
  import Json4sSupportSpec._

  private implicit val system: ActorSystem          = ActorSystem()
  private implicit val formats: DefaultFormats.type = DefaultFormats

  private val foo = Foo("bar")

  "Json4sSupport" should {
    "enable marshalling and unmarshalling objects for `DefaultFormats` and `jackson.Serialization`" in {
      implicit val serialization: Serialization.type = jackson.Serialization
      Marshal(foo)
        .to[RequestEntity]
        .flatMap(Unmarshal(_).to[Foo])
        .map(_ shouldBe foo)
    }

    "enable streamed marshalling and unmarshalling for json arrays" in {
      implicit val serialization: Serialization.type = jackson.Serialization

      val foos = (0 to 100).map(i => Foo(s"bar-$i")).toList

      Marshal(Source(foos))
        .to[RequestEntity]
        .flatMap { entity =>
          Unmarshal(entity).to[String].onComplete(println)

          Unmarshal(entity).to[SourceOf[Foo]]
        }
        .flatMap(_.runWith(Sink.seq))
        .map(_ shouldBe foos)
    }

    "enable marshalling and unmarshalling objects for `DefaultFormats` and `native.Serialization`" in {
      implicit val serialization: native.Serialization.type = native.Serialization
      Marshal(foo)
        .to[RequestEntity]
        .flatMap(Unmarshal(_).to[Foo])
        .map(_ shouldBe foo)
    }

    "provide proper error messages for requirement errors" in {
      implicit val serialization: native.Serialization.type = native.Serialization
      val entity =
        HttpEntity(MediaTypes.`application/json`, """{ "bar": "baz" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(_ should have message "requirement failed: bar must start with 'bar'!")
    }

    "fail with NoContentException when unmarshalling empty entities" in {
      implicit val serialization: native.Serialization.type = native.Serialization
      val entity                                            = HttpEntity.empty(`application/json`)
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(_ shouldBe Unmarshaller.NoContentException)
    }

    "fail with UnsupportedContentTypeException when Content-Type is not `application/json`" in {
      implicit val serialization: native.Serialization.type = native.Serialization
      val entity                                            = HttpEntity("""{ "bar": "bar" }""")
      Unmarshal(entity)
        .to[Foo]
        .failed
        .map(
          _ shouldBe UnsupportedContentTypeException(Some(`text/plain(UTF-8)`), `application/json`)
        )
    }

    "allow unmarshalling with passed in Content-Types" in {
      implicit val serialization: native.Serialization.type = native.Serialization
      val foo                                               = Foo("bar")
      val `application/json-home` =
        MediaType.applicationWithFixedCharset("json-home", HttpCharsets.`UTF-8`, "json-home")

      final object CustomJson4sSupport extends Json4sSupport {
        override def unmarshallerContentTypes = List(`application/json`, `application/json-home`)
      }
      import CustomJson4sSupport._

      val entity = HttpEntity(`application/json-home`, """{ "bar": "bar" }""")
      Unmarshal(entity).to[Foo].map(_ shouldBe foo)
    }
  }

  override protected def afterAll() = {
    Await.ready(system.terminate(), 42.seconds)
    super.afterAll()
  }
}
