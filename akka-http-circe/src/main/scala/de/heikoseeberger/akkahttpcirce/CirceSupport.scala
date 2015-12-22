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
import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import cats.data.Xor
import io.circe.{ jawn, Encoder, Decoder, Json, Printer }

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
 * To use automatic codec derivation, user need to import `circe.generic.auto._`
 */
object CirceSupport extends CirceSupport

/**
 * JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
 * To use automatic codec derivation, user need to import `circe.generic.auto._`
 */
trait CirceSupport {

  /**
   * HTTP Request => `T`
   *
   * @param decoder decoder for `T`, probably created by `circe.generic`
   * @tparam T class to decode
   * @return unmarshaller for `T`
   */
  implicit def circeUnmarshaller[T](implicit decoder: Decoder[T]): FromEntityUnmarshaller[T] =
    circeJsonUnmarshaller.map { json =>
      decoder.decodeJson(json) match {
        case Xor.Right(entity) => entity
        case Xor.Left(e)       => throw e
      }
    }

  /**
   * HTTP Request => Json
   *
   * @return unmarshaller for Circe Json
   */
  implicit def circeJsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input =
          if (charset == HttpCharsets.`UTF-8`) data.utf8String
          else data.decodeString(charset.nioCharset.name)
        jawn.parse(input) match {
          case Xor.Right(json) => json
          case Xor.Left(e)     => throw e
        }
      }

  /**
   * Json => HTTP Response
   *
   * @param printer pretty printer function
   * @return marshaller for any Json value
   */
  implicit def circeJsonMarshaller(implicit printer: Json => String = Printer.noSpaces.pretty): ToEntityMarshaller[Json] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)

  /**
   * `T` => HTTP Response
   *
   * @param encoder encoder for `T`, probably created by `circe.generic`
   * @param printer pretty printer function
   * @tparam T class to encode
   * @return marshaller for any Json value
   */
  implicit def circeToEntityMarshaller[T](implicit encoder: Encoder[T], printer: Json => String = Printer.noSpaces.pretty): ToEntityMarshaller[T] =
    circeJsonMarshaller.compose(encoder.apply)
}
