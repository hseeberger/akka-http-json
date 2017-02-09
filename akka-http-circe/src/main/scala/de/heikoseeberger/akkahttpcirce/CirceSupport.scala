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

package de.heikoseeberger.akkahttpcirce

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import io.circe.{ jawn, Decoder, Encoder, Json, Printer }

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
  *
  * To use automatic codec derivation, user need to import `circe.generic.auto._`.
  */
object CirceSupport extends CirceSupport

/**
  * JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
  *
  * To use automatic codec derivation, user need to import `io.circe.generic.auto._`
  */
trait CirceSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller = Marshaller.stringMarshaller(`application/json`)

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(jawn.decode(_).fold(throw _, identity))

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def marshaller[A: Encoder]: ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(Printer.noSpaces.pretty).compose(implicitly[Encoder[A]].apply)
}
