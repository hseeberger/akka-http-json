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

package de.heikoseeberger.akkahttpziojson

import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{ Marshaller, Marshalling, ToEntityMarshaller }
import akka.http.scaladsl.model.{
  ContentType,
  ContentTypeRange,
  HttpEntity,
  MediaType,
  MessageEntity
}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshal, Unmarshaller }
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal
import zio.json._

/**
  * JSON marshalling/unmarshalling using zio-json codec implicits.
  *
  * The opaque marshaller writes `A` to JSON.
  *
  * The unmarshaller follows zio-json's early exit strategy.
  */
trait ZioJsonSupport {
  type SourceOf[A] = Source[A, _]

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)

  private def sourceByteStringMarshaller(
      mediaType: MediaType.WithFixedCharset
  ): Marshaller[SourceOf[ByteString], MessageEntity] =
    Marshaller[SourceOf[ByteString], MessageEntity] { implicit ec => value =>
      try FastFuture.successful {
        Marshalling.WithFixedContentType(
          mediaType,
          () => HttpEntity(contentType = mediaType, data = value)
        ) :: Nil
      } catch {
        case NonFatal(e) => FastFuture.failed(e)
      }
    }

  private val jsonSourceStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(sourceByteStringMarshaller)

  private def jsonSource[A](entitySource: SourceOf[A])(implicit
      encoder: JsonEncoder[A],
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(_.toJson)
      .map(ByteString(_))
      .via(support.framingRenderer)

  /**
    * `ByteString` => `A`
    *
    * @tparam A type to decode
    * @return
    */
  implicit final def fromByteStringUnmarshaller[A: JsonDecoder]: Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.fromTry(bs.toString.fromJson.left.map(new Throwable(_)).toTry))

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit final def marshaller[A: JsonEncoder]: ToEntityMarshaller[A] =
    Marshaller.opaque(_.toJson)

  /**
    * HTTPEntity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit final def unmarshaller[A: JsonDecoder]: FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .flatMap { implicit ec => implicit m => a =>
        a match {
          case ByteString.empty => throw Unmarshaller.NoContentException
          case data =>
            val marshaller = fromByteStringUnmarshaller
            marshaller(data)
        }
      }

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A type to decode
    * @return unmarshaller from `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: JsonDecoder](implicit
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): FromEntityUnmarshaller[SourceOf[A]] =
    Unmarshaller
      .withMaterializer[HttpEntity, SourceOf[A]] { implicit ec => implicit mat => entity =>
        def asyncParse(bs: ByteString) =
          Unmarshal(bs).to[A]

        def ordered =
          Flow[ByteString].mapAsync(support.parallelism)(asyncParse)

        def unordered =
          Flow[ByteString].mapAsyncUnordered(support.parallelism)(asyncParse)

        Future.successful {
          entity.dataBytes
            .via(support.framingDecoder)
            .via(if (support.unordered) unordered else ordered)
        }
      }
      .forContentTypes(unmarshallerContentTypes: _*)

  /**
    * `SourceOf[A]` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `SourceOf[A]` value
    */
  implicit def sourceMarshaller[A](implicit
      writes: JsonEncoder[A],
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
