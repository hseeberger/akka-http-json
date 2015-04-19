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

package de.heikoseeberger.akkahttpjsonplay

import akka.actor.ActorSystem
import akka.http.marshalling.Marshal
import akka.http.model.RequestEntity
import akka.http.unmarshalling.Unmarshal
import akka.stream.ActorFlowMaterializer
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import play.api.libs.json.{ JsResult, JsSuccess, Json }
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object PlayJsonMarshallingSpec {

  case class Foo(bar: String)

  implicit val fooFormat = Json.format[Foo]
}

class PlayJsonMarshallingSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  import PlayJsonMarshalling._
  import PlayJsonMarshallingSpec._
  import system.dispatcher

  implicit val system = ActorSystem()
  implicit val mat = ActorFlowMaterializer()

  "PlayJsonMarshalling" should {

    "enable marshalling and unmarshalling objects for which Writes and Reads exist" in {
      val foo = Foo("bar")
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100 millis)
      Await.result(Unmarshal(entity).to[Foo], 100 millis) shouldBe foo
    }

    "enable unmarshalling to JsSuccess for objects for which a Reads exist" in {
      val foo = Foo("bar")
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100 millis)
      Await.result(Unmarshal(entity).to[JsResult[Foo]], 100 millis).asOpt shouldBe Some(foo)
    }

    "enable unmarshalling to JsError for objects for which a Reads exist" in {
      val foo = Json.parse("""{ "bar": 1 }""")
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100 millis)
      Await.result(Unmarshal(entity).to[JsResult[Foo]], 100 millis).isError shouldBe true
    }
  }

  override protected def afterAll() = {
    system.shutdown()
    system.awaitTermination()
    super.afterAll()
  }
}
