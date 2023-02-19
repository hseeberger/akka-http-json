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
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest
import org.apache.pekko.http.scaladsl.server.Directives
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.Source
import org.json4s.jackson.Serialization
import org.json4s.{ DefaultFormats, jackson }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object ExampleApp {

  final case class Foo(bar: String)

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()

    Http().newServerAt("127.0.0.1", 8000).bindFlow(route)

    StdIn.readLine("Hit ENTER to exit")
    Await.ready(system.terminate(), Duration.Inf)
  }

  def route(implicit sys: ActorSystem) = {
    import Directives._
    import Json4sSupport._

    implicit val serialization: Serialization.type =
      jackson.Serialization // or native.Serialization
    implicit val formats: DefaultFormats.type = DefaultFormats

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
