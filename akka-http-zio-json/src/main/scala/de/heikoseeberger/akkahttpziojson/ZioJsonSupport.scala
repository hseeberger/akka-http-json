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

import org.apache.pekko.http.javadsl.common.JsonEntityStreamingSupport
import org.apache.pekko.http.scaladsl.common.EntityStreamingSupport
import org.apache.pekko.http.scaladsl.marshalling.{ Marshaller, Marshalling, ToEntityMarshaller }
import org.apache.pekko.http.scaladsl.model.{
  ContentType,
  ContentTypeRange,
  HttpEntity,
  MediaType,
  MessageEntity
}
import org.apache.pekko.http.scaladsl.model.MediaTypes.`application/json`
import org.apache.pekko.http.scaladsl.unmarshalling.{
  FromEntityUnmarshaller,
  Unmarshal,
  Unmarshaller
}
import org.apache.pekko.http.scaladsl.util.FastFuture
import org.apache.pekko.stream.scaladsl.{ Flow, Source }
import org.apache.pekko.util.ByteString
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.control.NonFatal
import zio.json._
import zio.stream.ZStream
import zio.Unsafe

object ZioJsonSupport extends ZioJsonSupport

/**
  * JSON marshalling/unmarshalling using zio-json codec implicits.
  *
  * The marshaller writes `A` to JSON `HTTPEntity`.
  *
  * The unmarshaller follows zio-json's early exit strategy, attempting to reading JSON to an `A`.
  *
  * A safe unmarshaller is provided to attempt reading JSON to an `Either[String, A]` instead.
  *
  * No intermediate JSON representation as per zio-json's design.
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
      try
        FastFuture.successful {
          Marshalling.WithFixedContentType(
            mediaType,
            () => HttpEntity(contentType = mediaType, data = value)
          ) :: Nil
        }
      catch {
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
    * @tparam A
    *   type to decode
    * @return
    */
  implicit final def fromByteStringUnmarshaller[A](implicit
      jd: JsonDecoder[A],
      rt: zio.Runtime[Any]
  ): Unmarshaller[ByteString, A] =
    Unmarshaller(_ =>
      bs => {
        val decoded = jd.decodeJsonStreamInput(ZStream.fromIterable(bs))
        Unsafe.unsafeCompat(implicit u => rt.unsafe.runToFuture(decoded))
      }
    )

  /**
    * `A` => HTTP entity
    *
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `A` value
    */
  implicit final def marshaller[A: JsonEncoder]: ToEntityMarshaller[A] =
    Marshaller.oneOf(mediaTypes: _*) { mediaType =>
      Marshaller.withFixedContentType(ContentType(mediaType)) { a =>
        HttpEntity(mediaType, ByteString(a.toJson))
      }
    }

  /**
    * HTTPEntity => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `A`
    */
  implicit final def unmarshaller[A: JsonDecoder, RT: zio.Runtime]: FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .flatMap { implicit ec => implicit m =>
        {
          case ByteString.empty => throw Unmarshaller.NoContentException
          case data =>
            val marshaller = fromByteStringUnmarshaller
            marshaller(data)
        }
      }

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller from `Source[A, _]`
    */
  implicit final def sourceUnmarshaller[A: JsonDecoder, RT: zio.Runtime](implicit
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
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `SourceOf[A]` value
    */
  implicit final def sourceMarshaller[A](implicit
      writes: JsonEncoder[A],
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])

  implicit final def safeUnmarshaller[A: JsonDecoder]: FromEntityUnmarshaller[Either[String, A]] =
    Unmarshaller.stringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map(_.fromJson)
}
