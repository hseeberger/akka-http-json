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

package de.heikoseeberger.akkahttpavro4s

import java.io.ByteArrayOutputStream

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentType, ContentTypeRange, HttpEntity, MediaType }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import com.sksamuel.avro4s._
import org.apache.avro.Schema

import scala.collection.immutable.Seq

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
object AvroSupport extends AvroSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
trait AvroSupport {
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
    val schema      = AvroSchema[A]
    val mediaType   = mediaTypes.head
    val contentType = ContentType.WithFixedCharset(mediaType)
    Marshaller.withFixedContentType(contentType) { obj =>
      HttpEntity.Strict(
        contentType,
        ByteString.fromArrayUnsafe {
          val baos   = new ByteArrayOutputStream()
          val stream = AvroOutputStream.json[A].to(baos).build(schema)
          stream.write(obj)
          stream.close()
          baos.toByteArray
        }
      )
    }
  }
}
