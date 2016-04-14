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
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import io.circe._

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
 *
 * To use automatic codec derivation, user need to import `circe.generic.auto._`.
 */
object CirceSupport extends CirceSupport

/**
 * JSON marshalling/unmarshalling using an in-scope *Circe* protocol.
 *
 * To use automatic codec derivation, user need to import `circe.generic.auto._`
 */
trait CirceSupport {

  implicit def circeUnmarshallerConverter[A](decoder: Decoder[A]): FromEntityUnmarshaller[A] =
    circeUnmarshaller(decoder)

  /**
   * HTTP entity => `A`
   *
   * @param decoder decoder for `A`, probably created by `circe.generic`
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def circeUnmarshaller[A](implicit decoder: Decoder[A]): FromEntityUnmarshaller[A] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        jawn.decode(data.decodeString(charset.nioCharset.name)) valueOr (throw _)
      }

  implicit def circeToEntityMarshallerConverter[A](encoder: Encoder[A])(implicit printer: Json => String = Printer.noSpaces.pretty): ToEntityMarshaller[A] =
    circeToEntityMarshaller(encoder)

  /**
   * `A` => HTTP entity
   *
   * @param encoder encoder for `A`, probably created by `circe.generic`
   * @param printer pretty printer function
   * @tparam A type to encode
   * @return marshaller for any `A` value
   */
  implicit def circeToEntityMarshaller[A](implicit encoder: Encoder[A], printer: Json => String = Printer.noSpaces.pretty): ToEntityMarshaller[A] =
    circeJsonMarshaller.compose(encoder.apply)

  /**
   * JSON => HTTP entity
   *
   * @param printer pretty printer function
   * @return marshaller for any Json value
   */
  implicit def circeJsonMarshaller(implicit printer: Json => String = Printer.noSpaces.pretty): ToEntityMarshaller[Json] =
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(printer)
}
