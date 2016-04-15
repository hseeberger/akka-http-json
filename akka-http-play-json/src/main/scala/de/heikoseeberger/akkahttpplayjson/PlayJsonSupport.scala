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

package de.heikoseeberger.akkahttpplayjson

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import play.api.libs.json.{ JsResultException, JsValue, Json, Reads, Writes }

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *play-json* protocol.
 */
object PlayJsonSupport extends PlayJsonSupport

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *play-json* protocol.
 */
trait PlayJsonSupport {

  /**
   * HTTP entity => `A`
   *
   * @param reads reader for `A`
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def playJsonUnmarshaller[A](implicit reads: Reads[A]): FromEntityUnmarshaller[A] = {
    def read(json: JsValue) = reads.reads(json).recoverTotal(error => throw JsResultException(error.errors))
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset((data, charset) => read(Json.parse(data.decodeString(charset.nioCharset.name))))
  }

  /**
   * `A` => HTTP entity
   *
   * @param writes writer for `A`
   * @param printer pretty printer function
   * @tparam A type to encode
   * @return marshaller for any `A` value
   */
  implicit def playJsonMarshaller[A](implicit writes: Writes[A], printer: JsValue => String = Json.prettyPrint): ToEntityMarshaller[A] =
    Marshaller.StringMarshaller.wrap(`application/json`)(printer).compose(writes.writes)
}
