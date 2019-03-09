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

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ ContentType, ContentTypeRange, HttpEntity, MediaType }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import com.github.plokhotnyuk.jsoniter_scala.core._

import scala.collection.immutable.Seq

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope instance of JsonValueCodec
  */
object JsoniterScalaSupport extends JsoniterScalaSupport {
  val defaultReaderConfig: ReaderConfig =
    ReaderConfig(preferredBufSize = 100 * 1024, preferredCharBufSize = 10 * 1024)
  val defaultWriterConfig: WriterConfig = WriterConfig(preferredBufSize = 100 * 1024)
}

/**
  * JSON marshalling/unmarshalling using an in-scope instance of JsonValueCodec
  */
trait JsoniterScalaSupport {
  import JsoniterScalaSupport._
  private val defaultMediaTypes: Seq[MediaType.WithFixedCharset] = List(`application/json`)
  private val defaultContentTypes: Seq[ContentTypeRange] =
    defaultMediaTypes.map(ContentTypeRange.apply)
  private val byteArrayUnmarshaller: FromEntityUnmarshaller[Array[Byte]] =
    Unmarshaller.byteArrayUnmarshaller.forContentTypes(unmarshallerContentTypes: _*)

  def unmarshallerContentTypes: Seq[ContentTypeRange] = defaultContentTypes

  def mediaTypes: Seq[MediaType.WithFixedCharset] = defaultMediaTypes

  /**
    * HTTP entity => `A`
    */
  implicit def unmarshaller[A](
      implicit codec: JsonValueCodec[A],
      config: ReaderConfig = defaultReaderConfig
  ): FromEntityUnmarshaller[A] =
    byteArrayUnmarshaller.map { bytes =>
      if (bytes.length == 0) throw Unmarshaller.NoContentException
      readFromArray[A](bytes, config)
    }

  /**
    * `A` => HTTP entity
    */
  implicit def marshaller[A](
      implicit codec: JsonValueCodec[A],
      config: WriterConfig = defaultWriterConfig
  ): ToEntityMarshaller[A] = {
    val mediaType   = mediaTypes.head
    val contentType = ContentType.WithFixedCharset(mediaType)
    Marshaller.withFixedContentType(contentType) { obj =>
      HttpEntity.Strict(contentType, ByteString.fromArrayUnsafe(writeToArray(obj, config)))
    }
  }
}
