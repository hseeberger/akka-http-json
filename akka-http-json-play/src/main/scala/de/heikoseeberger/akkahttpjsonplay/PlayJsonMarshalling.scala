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

import akka.http.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.model.MediaTypes
import akka.http.unmarshalling.{ FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers }
import akka.stream.FlowMaterializer
import play.api.libs.json.{ Json, Reads, Writes }
import scala.concurrent.ExecutionContext

object PlayJsonMarshalling extends PlayJsonMarshalling

/** Play JSON integration for Akka HTTP (un)marshalling. */
trait PlayJsonMarshalling {

  /** `FromEntityUnmarshaller` for `application/json` depending on a Play JSON `Reads`. */
  implicit def unmarshaller[A](implicit reads: Reads[A], ec: ExecutionContext, mat: FlowMaterializer): FromEntityUnmarshaller[A] =
    PredefinedFromEntityUnmarshallers
      .stringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .map(s => reads.reads(Json.parse(s)).get)

  /** `ToEntityMarshaller` to `application/json` depending on a Play JSON `Writes`. */
  implicit def marshaller[A](implicit writes: Writes[A]): ToEntityMarshaller[A] =
    PredefinedToEntityMarshallers
      .stringMarshaller(MediaTypes.`application/json`)
      .compose(a => writes.writes(a).toString)
}
