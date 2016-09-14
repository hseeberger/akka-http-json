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

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{
  FromEntityUnmarshaller,
  Unmarshaller
}
import akka.util.ByteString
import upickle.default.{ Reader, Writer, readJs, writeJs }
import upickle.{ Js, json }

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
object UpickleSupport extends UpickleSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
trait UpickleSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller =
    Marshaller.stringMarshaller(`application/json`)

  /**
    * HTTP entity => `A`
    *
    * @param reader reader for `A`
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def upickleUnmarshaller[A](
      implicit reader: Reader[A]
  ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => readJs[A](json.read(data)))

  /**
    * `A` => HTTP entity
    *
    * @param writer writer for `A`
    * @param printer pretty printer function
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def upickleMarshaller[A](
      implicit writer: Writer[A],
      printer: Js.Value => String = json.write(_, 0)
  ): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(printer).compose(writeJs[A])
}
