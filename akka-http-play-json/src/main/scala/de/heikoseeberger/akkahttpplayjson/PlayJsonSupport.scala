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

package de.heikoseeberger.akkahttpplayjson

import org.apache.pekko.http.javadsl.common.JsonEntityStreamingSupport
import org.apache.pekko.http.scaladsl.common.EntityStreamingSupport
import org.apache.pekko.http.scaladsl.marshalling._
import org.apache.pekko.http.scaladsl.model.{
  ContentTypeRange,
  HttpEntity,
  MediaType,
  MessageEntity
}
import org.apache.pekko.http.scaladsl.model.MediaTypes.`application/json`
import org.apache.pekko.http.scaladsl.unmarshalling._
import org.apache.pekko.http.scaladsl.util.FastFuture
import org.apache.pekko.stream.scaladsl.{ Flow, Source }
import org.apache.pekko.util.ByteString
import play.api.libs.json.{ JsError, JsResultException, JsValue, Json, Reads, Writes }
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *play-json* protocol.
  */
object PlayJsonSupport extends PlayJsonSupport {
  final case class PlayJsonError(error: JsError) extends IllegalArgumentException {
    override def getMessage: String =
      JsError.toJson(error).toString()
  }
}

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *play-json* protocol.
  */
trait PlayJsonSupport {
  type SourceOf[A] = Source[A, _]

  import PlayJsonSupport._

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

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

  private def read[A: Reads](json: JsValue): A =
    implicitly[Reads[A]]
      .reads(json)
      .recoverTotal(e => throw PlayJsonError(e))

  private def jsonSource[A](entitySource: SourceOf[A])(implicit
      writes: Writes[A],
      printer: JsValue => String,
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(writes.writes)
      .map(printer)
      .map(ByteString(_))
      .via(support.framingRenderer)

  /**
    * HTTP entity => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `A`
    */
  implicit def unmarshaller[A: Reads]: FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => read(Json.parse(data)))

  /**
    * `A` => HTTP entity
    *
    * @tparam A
    *   type to encode
    * @return
    *   marshaller for any `A` value
    */
  implicit def marshaller[A](implicit
      writes: Writes[A],
      printer: JsValue => String = Json.prettyPrint
  ): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(printer).compose(writes.writes)

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: Reads]: Unmarshaller[ByteString, A] =
    Unmarshaller(_ => bs => Future.fromTry(Try(Json.parse(bs.toArray).as[A])))

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: Reads](implicit
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
            .recoverWithRetries(
              1,
              { case a: JsResultException =>
                Source.failed(PlayJsonError(JsError(a.errors)))
              }
            )
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
      writes: Writes[A],
      printer: JsValue => String = Json.stringify,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}
