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

package de.heikoseeberger.akkahttpjson4s

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
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{ Flow, Source }
import org.apache.pekko.util.ByteString
import de.heikoseeberger.akkahttpjson4s.Json4sSupport.ShouldWritePretty.False
import java.lang.reflect.InvocationTargetException
import org.json4s.{ Formats, MappingException, Serialization }
import scala.collection.immutable.Seq
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import scala.util.control.NonFatal

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
  *
  * Pretty printing is enabled if an implicit [[Json4sSupport.ShouldWritePretty.True]] is in scope.
  */
object Json4sSupport extends Json4sSupport {

  sealed abstract class ShouldWritePretty

  final object ShouldWritePretty {
    final object True  extends ShouldWritePretty
    final object False extends ShouldWritePretty
  }
}

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
  *
  * Pretty printing is enabled if an implicit [[Json4sSupport.ShouldWritePretty.True]] is in scope.
  */
trait Json4sSupport {
  type SourceOf[A] = Source[A, _]

  import Json4sSupport._

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

  private def jsonSource[A <: AnyRef](entitySource: SourceOf[A])(implicit
      f: Formats,
      s: Serialization,
      isPretty: ShouldWritePretty,
      support: JsonEntityStreamingSupport
  ): SourceOf[ByteString] =
    entitySource
      .map(e =>
        if (isPretty == False) s.write[A](e)
        else s.writePretty[A](e)
      )
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
  implicit def unmarshaller[A: Manifest](implicit
      serialization: Serialization,
      formats: Formats
  ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller
      .map(s => serialization.read(s))
      .recover(throwCause)

  /**
    * `A` => HTTP entity
    *
    * @tparam A
    *   type to encode, must be upper bounded by `AnyRef`
    * @return
    *   marshaller for any `A` value
    */
  implicit def marshaller[A <: AnyRef](implicit
      serialization: Serialization,
      formats: Formats,
      shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False
  ): ToEntityMarshaller[A] =
    shouldWritePretty match {
      case ShouldWritePretty.False =>
        jsonStringMarshaller.compose(serialization.write[A])
      case ShouldWritePretty.True =>
        jsonStringMarshaller.compose(serialization.writePretty[A])
    }

  /**
    * `ByteString` => `A`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for any `A` value
    */
  implicit def fromByteStringUnmarshaller[A: Manifest](implicit
      s: Serialization,
      formats: Formats
  ): Unmarshaller[ByteString, A] = {
    val result: Unmarshaller[ByteString, A] =
      Unmarshaller(_ => bs => Future.fromTry(Try(s.read(bs.utf8String))))

    result.recover(throwCause)
  }

  /**
    * HTTP entity => `Source[A, _]`
    *
    * @tparam A
    *   type to decode
    * @return
    *   unmarshaller for `Source[A, _]`
    */
  implicit def sourceUnmarshaller[A: Manifest](implicit
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json(),
      serialization: Serialization,
      formats: Formats
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
  implicit def sourceMarshaller[A <: AnyRef](implicit
      serialization: Serialization,
      formats: Formats,
      shouldWritePretty: ShouldWritePretty = False,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  ): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])

  private def throwCause[A](
      ec: ExecutionContext
  )(mat: Materializer): PartialFunction[Throwable, A] = {
    case e: MappingException if e.cause.isInstanceOf[InvocationTargetException] =>
      throw e.cause.getCause
  }
}
