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

package de.heikoseeberger.akkahttpargonaut

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import argonaut.{ DecodeJson, EncodeJson, Json, Parse, PrettyParams }
import scala.collection.immutable.Seq

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
  *
  * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`.
  */
object ArgonautSupport extends ArgonautSupport

/**
  * JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
  *
  * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`
  */
trait ArgonautSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: DecodeJson]: FromEntityUnmarshaller[A] = {
    def parse(s: String) =
      Parse.parse(s) match {
        case Right(json)   => json
        case Left(message) => sys.error(message)
      }
    def decode(json: Json) =
      implicitly[DecodeJson[A]].decodeJson(json).result match {
        case Right(entity) => entity
        case Left((m, h))  => sys.error(m + " - " + h)
      }
    jsonStringUnmarshaller.map(parse).map(decode)
  }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def marshaller[A: EncodeJson]: ToEntityMarshaller[A] =
    jsonStringMarshaller
      .compose(PrettyParams.nospace.pretty)
      .compose(implicitly[EncodeJson[A]].apply)
}
