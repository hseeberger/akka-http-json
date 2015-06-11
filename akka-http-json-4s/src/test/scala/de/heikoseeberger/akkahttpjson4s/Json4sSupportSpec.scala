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

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorFlowMaterializer
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import org.json4s._
object Json4sSupportSpec {
  case class Foo(bar: String)
}

class Json4sSupportSpec extends WordSpec with Matchers with BeforeAndAfterAll {

  import Json4sSupportSpec._
  import system.dispatcher
  import Json4sSupport.{ json4sMarshaller, json4sUnmarshaller }

  implicit val system = ActorSystem()
  implicit val mat = ActorFlowMaterializer()

  val foo = Foo("bar")

  "Json4sSupport" should {

    "enable marshalling and unmarshalling objects for default `Formats` and `jackson.Serialization`" in {
      implicit val serialization = jackson.Serialization
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100 millis)
      Await.result(Unmarshal(entity).to[Foo], 100 millis) shouldBe foo
    }
    "enable marshalling and unmarshalling objects for default `Formats` and `native.Serialization`" in {
      implicit val serialization = native.Serialization
      val entity = Await.result(Marshal(foo).to[RequestEntity], 100 millis)
      Await.result(Unmarshal(entity).to[Foo], 100 millis) shouldBe foo
    }
  }

  override protected def afterAll() = {
    system.shutdown()
    system.awaitTermination()
    super.afterAll()
  }
}
