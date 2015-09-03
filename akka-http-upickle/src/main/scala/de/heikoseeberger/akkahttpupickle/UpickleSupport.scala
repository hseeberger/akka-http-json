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
import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.stream.Materializer
import upickle.{ Js, json }
import upickle.default.{ Reader, Writer, readJs, writeJs }

/**
 * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
 */
object UpickleSupport extends UpickleSupport

/**
 * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
 */
trait UpickleSupport {

  implicit def upickleUnmarshallerConverter[A](reader: Reader[A])(implicit mat: Materializer): FromEntityUnmarshaller[A] =
    upickleUnmarshaller(reader, mat)

  implicit def upickleUnmarshaller[A](implicit reader: Reader[A], mat: Materializer): FromEntityUnmarshaller[A] =
    upickleJsValueUnmarshaller.map(readJs[A])

  implicit def upickleJsValueUnmarshaller(implicit mat: Materializer): FromEntityUnmarshaller[Js.Value] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        json.read(input)
      }

  implicit def upickleMarshallerConverter[A](writer: Writer[A])(implicit printer: Js.Value => String = json.write): ToEntityMarshaller[A] =
    upickleMarshaller[A](writer)

  implicit def upickleMarshaller[A](implicit writer: Writer[A], printer: Js.Value => String = json.write): ToEntityMarshaller[A] =
    upickleJsValueMarshaller.compose(writeJs[A])

  implicit def upickleJsValueMarshaller(implicit printer: Js.Value => String = json.write): ToEntityMarshaller[Js.Value] =
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`)(printer)
}
