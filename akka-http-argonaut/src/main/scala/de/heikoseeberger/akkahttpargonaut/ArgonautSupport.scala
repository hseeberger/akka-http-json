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

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
 * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`
 */
object ArgonautSupport extends ArgonautSupport

/**
 * JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
 * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`
 */
trait ArgonautSupport {

  /**
   * HTTP Request => `T`
   *
   * @param decoder decoder for `T`
   * @tparam T class to decode
   * @return unmarshaller for `T`
   */
  implicit def argonautUnmarshaller[T](implicit decoder: DecodeJson[T]): FromEntityUnmarshaller[T] =
    argonautJsonUnmarshaller.map { json =>
      decoder.decodeJson(json).result.toEither match {
        case Right(entity)      => entity
        case Left((message, _)) => sys.error(message)
      }
    }

  /**
   * HTTP Request => Json
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

  /**
   * Json => HTTP Response
   *
   * @param printer pretty printer function
   * @return marshaller for any Json value
   */
  implicit def argonautJsonMarshaller(implicit printer: Json => String = PrettyParams.nospace.pretty): ToEntityMarshaller[Json] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)

  /**
   * `T` => HTTP Response
   *
   * @param encoder encoder for `T`
   * @param printer pretty printer function
   * @tparam T class to encode
   * @return marshaller for any `T` value
   */
  implicit def argonautToEntityMarshaller[T](implicit encoder: EncodeJson[T], printer: Json => String = PrettyParams.nospace.pretty): ToEntityMarshaller[T] =
    argonautJsonMarshaller.compose(encoder.apply)
}
