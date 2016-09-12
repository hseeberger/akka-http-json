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
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{
  FromEntityUnmarshaller,
  Unmarshaller
}
import akka.util.ByteString
import argonaut.{ DecodeJson, EncodeJson, Json, Parse, PrettyParams }

import scalaz.Scalaz._

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

  private val jsonStringUnmarshaller: FromEntityUnmarshaller[String] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset {
        case (ByteString.empty, _) ⇒ throw Unmarshaller.NoContentException
        case (data, charset)       ⇒ data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller: ToEntityMarshaller[String] =
    Marshaller.stringMarshaller(`application/json`)

  /**
    * HTTP entity ⇒ `A`
    *
    * @param decoder decoder for `A`
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def argonautUnmarshaller[A](
      implicit decoder: DecodeJson[A]
  ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map { data ⇒
      Parse.parse(data).toEither match {
        case Right(json)   ⇒ json
        case Left(message) ⇒ sys.error(message)
      }
    }.map { json ⇒
      decoder.decodeJson(json).result.toEither match {
        case Right(entity) ⇒ entity
        case Left((message, history)) ⇒
          sys.error(message + " - " + history.shows)
      }
    }

  /**
    * `A` ⇒ HTTP entity
    *
    * @param encoder encoder for `A`
    * @param printer pretty printer function
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def argonautToEntityMarshaller[A](
      implicit encoder: EncodeJson[A],
      printer: Json ⇒ String = PrettyParams.nospace.pretty
  ): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(printer).compose(encoder.apply)

}
