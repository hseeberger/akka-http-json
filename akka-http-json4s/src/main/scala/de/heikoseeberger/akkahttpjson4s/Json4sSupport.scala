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

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.stream.FlowMaterializer
import org.json4s.{ Formats, Serialization }

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
 */
object Json4sSupport extends Json4sSupport

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
 */
trait Json4sSupport {

  implicit def json4sUnmarshallerConverter[A: Manifest](serialization: Serialization, formats: Formats)(implicit mat: FlowMaterializer): FromEntityUnmarshaller[A] =
    json4sUnmarshaller(manifest, serialization, formats, mat)

  implicit def json4sUnmarshaller[A: Manifest](implicit serialization: Serialization, formats: Formats, mat: FlowMaterializer): FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        serialization.read(input)
      }

  implicit def json4sMarshallerConverter[A <: AnyRef](serialization: Serialization, formats: Formats): ToEntityMarshaller[A] =
    json4sMarshaller(serialization, formats)

  implicit def json4sMarshaller[A <: AnyRef](implicit serialization: Serialization, formats: Formats): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`)(serialization.write[A])
}
