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

import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{ Marshaller, Marshalling, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypeRange, HttpEntity, MediaType, MessageEntity }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshal, Unmarshaller }
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString
import upickle.default.{ Reader, Writer, read, write }

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
object UpickleSupport extends UpickleSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
trait UpickleSupport {
  type SourceOf[A] = Source[A, _]

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

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

  private def jsonSource[A](entitySource: SourceOf[A])(
      implicit writes: Writer[A],
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(write(_))
      .map(ByteString(_))
      .via(support.framingRenderer)

  /**
    * `ByteString` => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: Reader]: Unmarshaller[ByteString, A] =
    Unmarshaller { _ => bs =>
      Future.fromTry(Try(read(bs.toArray)))
    }

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: Reader]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(read(_))

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def marshaller[A: Writer]: ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(write(_))

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A type to decode
    * @return unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: Reader](
      implicit support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
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
  implicit def sourceMarshaller[A](
      implicit writes: Writer[A],
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
