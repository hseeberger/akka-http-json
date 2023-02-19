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
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.marshalling.Marshal
import org.apache.pekko.http.scaladsl.model.{ HttpRequest, RequestEntity }
import org.apache.pekko.http.scaladsl.server.Directives
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.Source
import scala.concurrent.duration._
import scala.io.StdIn
import zio.json._

object ExampleApp {
  private final case class Foo(bar: String)
  private object Foo {
    implicit val fooEncoder: JsonEncoder[Foo] = DeriveJsonEncoder.gen
    implicit val fooDecoder: JsonDecoder[Foo] = DeriveJsonDecoder.gen
  }

  private implicit val rt: zio.Runtime[Any] = zio.Runtime.default

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()

    Http().newServerAt("127.0.0.1", 8000).bindFlow(route)

    StdIn.readLine("Hit ENTER to exit")
    system.terminate()
  }

  private def route(implicit sys: ActorSystem) = {
    import Directives._
    import ZioJsonSupport._

    pathSingleSlash {
      post {
        entity(as[Foo]) { foo =>
          complete {
            foo
          }
        }
      }
    } ~ pathPrefix("stream") {
      post {
        entity(as[SourceOf[Foo]]) { (fooSource: SourceOf[Foo]) =>
          import sys._

          Marshal(Source.single(Foo("a"))).to[RequestEntity]

          complete(fooSource.throttle(1, 2.seconds))
        }
      } ~ get {
        pathEndOrSingleSlash {
          complete(
            Source(0 to 5)
              .throttle(1, 1.seconds)
              .map(i => Foo(s"bar-$i"))
          )
        } ~ pathPrefix("remote") {
          onSuccess(Http().singleRequest(HttpRequest(uri = "http://localhost:8000/stream"))) {
            response => complete(Unmarshal(response).to[SourceOf[Foo]])
          }
        }
      }
    }
  }
}
