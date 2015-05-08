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

package de.heikoseeberger.akkahttpjson4snative

import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers }
import akka.stream.FlowMaterializer
import org.json4s.{ Serialization, DefaultFormats, Formats }
import scala.concurrent.ExecutionContext
import org.json4s._

object Json4sNativeMarshalling extends Json4sNativeMarshalling

/** json4s-native integration for Akka HTTP (un)marshalling. */
trait Json4sNativeMarshalling {

  /** `FromEntityUnmarshaller` for `application/json` depending on a json4s-native `DefaultFormats` and `native.Serialization`. */
  implicit def unmarshaller[A: Manifest](implicit formats: Formats = DefaultFormats, serialization: Serialization = native.Serialization, ec: ExecutionContext, mat: FlowMaterializer): FromEntityUnmarshaller[A] =
    PredefinedFromEntityUnmarshallers.stringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .map(serialization.read[A])

  /** `ToEntityMarshaller` to `application/json` depending on a json4s-native `DefaultFormats` and `native.Serialization`. */
  implicit def marshaller[A <: AnyRef: Manifest](implicit formats: Formats = DefaultFormats, serialization: Serialization = native.Serialization): ToEntityMarshaller[A] =
    PredefinedToEntityMarshallers.stringMarshaller(MediaTypes.`application/json`)
      .compose(serialization.write[A])
}
