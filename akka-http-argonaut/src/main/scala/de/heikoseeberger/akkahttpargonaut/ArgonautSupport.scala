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

package de.heikoseeberger.akkahttpargonaut

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
import argonaut.{ DecodeJson, EncodeJson, Json, Parse, PrettyParams }
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
  *
  * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`.
  */
object ArgonautSupport extends ArgonautSupport

/**
  * JSON marshalling/unmarshalling using an in-scope *Argonaut* protocol.
  *
  * To use automatic codec derivation, user needs to import `argonaut.Shapeless._`
  */
trait ArgonautSupport {
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

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

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
      e: EncodeJson[A],
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(e.apply)
      .map(PrettyParams.nospace.pretty)
      .map(ByteString(_))
      .via(support.framingRenderer)

  private def parse(s: String) =
    Parse.parse(s) match {
      case Right(json)   => json
      case Left(message) => sys.error(message)
    }

  private def decode[A: DecodeJson](json: Json) =
    DecodeJson.of[A].decodeJson(json).result match {
      case Right(entity) => entity
      case Left((m, h))  => sys.error(m + " - " + h)
    }

  /**
    * HTTP entity => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `A`
    */
  implicit def unmarshaller[A: DecodeJson]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(parse).map(decode[A])

  /**
    * `A` => HTTP entity
    *
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `A` value
    */
  implicit def marshaller[A: EncodeJson]: ToEntityMarshaller[A] =
    jsonStringMarshaller
      .compose(PrettyParams.nospace.pretty)
      .compose(EncodeJson.of[A].apply)

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: DecodeJson]: Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.successful(decode(parse(bs.utf8String))))

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: DecodeJson](implicit
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
      e: EncodeJson[A],
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
