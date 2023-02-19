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

package com.github.pjfanning.pekkohttpavro4s

import org.apache.pekko.http.javadsl.common.JsonEntityStreamingSupport
import org.apache.pekko.http.scaladsl.common.EntityStreamingSupport
import org.apache.pekko.http.scaladsl.marshalling.{ Marshaller, Marshalling, ToEntityMarshaller }
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.model.MediaTypes.`application/json`
import org.apache.pekko.http.scaladsl.unmarshalling.{
  FromEntityUnmarshaller,
  Unmarshal,
  Unmarshaller
}
import org.apache.pekko.http.scaladsl.util.FastFuture
import org.apache.pekko.stream.scaladsl.{ Flow, Source }
import org.apache.pekko.util.ByteString
import com.sksamuel.avro4s.{
  AvroInputStream,
  AvroOutputStream,
  AvroSchema,
  Decoder,
  Encoder,
  SchemaFor
}
import java.io.ByteArrayOutputStream
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
object AvroSupport extends AvroSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
trait AvroSupport {
  type SourceOf[A] = Source[A, _]

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

  private def jsonSource[A: SchemaFor: Encoder](entitySource: SourceOf[A])(implicit
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map { obj =>
        val baos   = new ByteArrayOutputStream()
        val stream = AvroOutputStream.json[A].to(baos).build()
        stream.write(obj)
        stream.close()
        baos.toByteArray
      }
      .map(ByteString(_))
      .via(support.framingRenderer)

  def unmarshallerContentTypes: Seq[ContentTypeRange] = defaultContentTypes

  def mediaTypes: Seq[MediaType.WithFixedCharset] = defaultMediaTypes

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: SchemaFor: Decoder]: Unmarshaller[ByteString, A] =
    Unmarshaller { _ => bs =>
      Future.fromTry {
        val schema = AvroSchema[A]

        Try {
          val bytes = bs.toArray
          if (bytes.length == 0) throw Unmarshaller.NoContentException
          AvroInputStream.json[A].from(bytes).build(schema).iterator.next()
        }
      }
    }

  /**
    * HTTP entity => `A`
    */
  implicit def unmarshaller[A: SchemaFor: Decoder]: FromEntityUnmarshaller[A] = {
    val schema = AvroSchema[A]
    byteArrayUnmarshaller.map { bytes =>
      if (bytes.length == 0) throw Unmarshaller.NoContentException
      AvroInputStream.json[A].from(bytes).build(schema).iterator.next()
    }
  }

  /**
    * `A` => HTTP entity
    */
  implicit def marshaller[A: SchemaFor: Encoder]: ToEntityMarshaller[A] = {
    val mediaType   = mediaTypes.head
    val contentType = ContentType.WithFixedCharset(mediaType)
    Marshaller.withFixedContentType(contentType) { obj =>
      HttpEntity.Strict(
        contentType,
        ByteString.fromArrayUnsafe {
          val baos   = new ByteArrayOutputStream()
          val stream = AvroOutputStream.json[A].to(baos).build()
          stream.write(obj)
          stream.close()
          baos.toByteArray
        }
      )
    }
  }

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: SchemaFor: Decoder](implicit
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
  implicit def sourceMarshaller[A: SchemaFor: Encoder](implicit
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
