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
import akka.http.scaladsl.model.{ HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
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

  implicit def argonautUnmarshallerConverter[A](decoder: DecodeJson[A]): FromEntityUnmarshaller[A] =
    argonautUnmarshaller(decoder)

  /**
   * HTTP entity => `A`
   *
   * @param decoder decoder for `A`
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def argonautUnmarshaller[A](implicit decoder: DecodeJson[A]): FromEntityUnmarshaller[A] =
    argonautJsonUnmarshaller.map { json =>
      decoder.decodeJson(json).result.toEither match {
        case Right(entity)            => entity
        case Left((message, history)) => sys.error(message + " - " + history.shows)
      }
    }

  /**
   * HTTP entity => JSON
   *
   * @return unmarshaller for Argonaut Json
   */
  implicit def argonautJsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input =
          if (charset == HttpCharsets.`UTF-8`) data.utf8String
          else data.decodeString(charset.nioCharset.name)
        Parse.parse(input).toEither match {
          case Right(json)   => json
          case Left(message) => sys.error(message)
        }
      }

  implicit def argonautToEntityMarshallerConverter[A](encoder: EncodeJson[A])(implicit printer: Json => String = PrettyParams.nospace.pretty): ToEntityMarshaller[A] =
    argonautToEntityMarshaller(encoder)

  /**
   * `A` => HTTP entity
   *
   * @param encoder encoder for `A`
   * @param printer pretty printer function
   * @tparam A type to encode
   * @return marshaller for any `A` value
   */
  implicit def argonautToEntityMarshaller[A](implicit encoder: EncodeJson[A], printer: Json => String = PrettyParams.nospace.pretty): ToEntityMarshaller[A] =
    argonautJsonMarshaller.compose(encoder.apply)

  /**
   * JSON => HTTP entity
   *
   * @param printer pretty printer function
   * @return marshaller for any Json value
   */
  implicit def argonautJsonMarshaller(implicit printer: Json => String = PrettyParams.nospace.pretty): ToEntityMarshaller[Json] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)
}
