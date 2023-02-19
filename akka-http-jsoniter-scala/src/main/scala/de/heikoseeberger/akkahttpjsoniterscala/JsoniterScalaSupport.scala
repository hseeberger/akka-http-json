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

package de.heikoseeberger.akkahttpjsoniterscala

import org.apache.pekko.http.javadsl.common.JsonEntityStreamingSupport
import org.apache.pekko.http.scaladsl.common.EntityStreamingSupport
import org.apache.pekko.http.scaladsl.marshalling._
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
import com.github.plokhotnyuk.jsoniter_scala.core._
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope instance of
  * JsonValueCodec
  */
object JsoniterScalaSupport extends JsoniterScalaSupport {
  val defaultReaderConfig: ReaderConfig =
    ReaderConfig.withPreferredBufSize(100 * 1024).withPreferredCharBufSize(10 * 1024)
  val defaultWriterConfig: WriterConfig = WriterConfig.withPreferredBufSize(100 * 1024)
}

/**
  * JSON marshalling/unmarshalling using an in-scope instance of JsonValueCodec
  */
trait JsoniterScalaSupport {
  type SourceOf[A] = Source[A, _]

  import JsoniterScalaSupport._

  private val defaultMediaTypes: Seq[MediaType.WithFixedCharset] = List(`application/json`)
  private val defaultContentTypes: Seq[ContentTypeRange] =
    defaultMediaTypes.map(ContentTypeRange.apply)
  private val byteArrayUnmarshaller: FromEntityUnmarshaller[Array[Byte]] =
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(unmarshallerContentTypes: _*)

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
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig,
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(writeToArray(_, config))
      .map(ByteString(_))
      .via(support.framingRenderer)

  def unmarshallerContentTypes: Seq[ContentTypeRange] = defaultContentTypes

  def mediaTypes: Seq[MediaType.WithFixedCharset] = defaultMediaTypes

  /**
    * HTTP entity => `A`
    */
  implicit def unmarshaller[A](implicit
      codec: JsonValueCodec[A],
      config: ReaderConfig = defaultReaderConfig
  ): FromEntityUnmarshaller[A] =
    byteArrayUnmarshaller.map { bytes =>
      if (bytes.length == 0) throw Unmarshaller.NoContentException
      readFromArray[A](bytes, config)
    }

  /**
    * `A` => HTTP entity
    */
  implicit def marshaller[A](implicit
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig
  ): ToEntityMarshaller[A] = {
    val mediaType   = mediaTypes.head
    val contentType = ContentType.WithFixedCharset(mediaType)
    Marshaller.withFixedContentType(contentType) { obj =>
      HttpEntity.Strict(contentType, ByteString.fromArrayUnsafe(writeToArray(obj, config)))
    }
  }

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A](implicit
      codec: JsonValueCodec[A],
      config: ReaderConfig = defaultReaderConfig
  ): Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.fromTry(Try(readFromArray(bs.toArray, config))))

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: JsonValueCodec](implicit
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json(),
      config: ReaderConfig = defaultReaderConfig
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
      codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
