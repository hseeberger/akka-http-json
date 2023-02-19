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

import org.apache.pekko.http.javadsl.common.JsonEntityStreamingSupport
import org.apache.pekko.http.scaladsl.common.EntityStreamingSupport
import org.apache.pekko.http.scaladsl.marshalling.{ Marshaller, Marshalling, ToEntityMarshaller }
import org.apache.pekko.http.scaladsl.model.{
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
import de.heikoseeberger.akkahttpupickle.UpickleCustomizationSupport._
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

// This companion object only exists for binary compatibility as adding methods with default implementations
// (including val's as they create synthetic methods) is not compatible.
private object UpickleCustomizationSupport {

  private def jsonStringUnmarshaller(support: UpickleCustomizationSupport) =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(support.unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private def jsonSourceStringMarshaller(support: UpickleCustomizationSupport) =
    Marshaller.oneOf(support.mediaTypes: _*)(support.sourceByteStringMarshaller)

  private def jsonStringMarshaller(support: UpickleCustomizationSupport) =
    Marshaller.oneOf(support.mediaTypes: _*)(Marshaller.stringMarshaller)
}

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
trait UpickleCustomizationSupport {
  type SourceOf[A] = Source[A, _]

  type Api <: upickle.Api

  def api: Api

  private lazy val apiInstance: Api = api

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

  private def jsonSource[A](entitySource: SourceOf[A])(implicit
      writes: apiInstance.Writer[A],
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(apiInstance.write(_))
      .map(ByteString(_))
      .via(support.framingRenderer)

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: apiInstance.Reader]: Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.fromTry(Try(apiInstance.read(bs.toArray))))

  /**
    * HTTP entity => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `A`
    */
  implicit def unmarshaller[A: apiInstance.Reader]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller(this).map(apiInstance.read(_))

  /**
    * `A` => HTTP entity
    *
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `A` value
    */
  implicit def marshaller[A: apiInstance.Writer]: ToEntityMarshaller[A] =
    jsonStringMarshaller(this).compose(apiInstance.write(_))

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: apiInstance.Reader](implicit
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
  implicit def sourceMarshaller[A](implicit
      writes: apiInstance.Writer[A],
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller(this).compose(jsonSource[A])
}
