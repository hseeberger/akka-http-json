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

package de.heikoseeberger.akkahttpcirce

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypeRange, HttpEntity }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import cats.data.NonEmptyList
import cats.syntax.show.toShowOps
import io.circe.{ jawn, Decoder, DecodingFailure, Encoder, Json, Printer }
import scala.collection.immutable.Seq

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller fails fast, throwing the first `Error` encountered.
  *
  * To use automatic codec derivation, user needs to import `io.circe.generic.auto._`.
  */
object FailFastCirceSupport extends FailFastCirceSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller fails fast, throwing the first `Error` encountered.
  *
  * To use automatic codec derivation import `io.circe.generic.auto._`.
  */
trait FailFastCirceSupport extends BaseCirceSupport with FailFastUnmarshaller

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller accumulates all errors in the exception `Errors`.
  *
  * To use automatic codec derivation, user needs to import `io.circe.generic.auto._`.
  */
object ErrorAccumulatingCirceSupport extends ErrorAccumulatingCirceSupport {
  final case class DecodingFailures(failures: NonEmptyList[DecodingFailure]) extends Exception {
    override def getMessage: String = failures.toList.map(_.show).mkString("\n")
  }
}

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller accumulates all errors in the exception `Errors`.
  *
  * To use automatic codec derivation import `io.circe.generic.auto._`.
  */
trait ErrorAccumulatingCirceSupport extends BaseCirceSupport with ErrorAccumulatingUnmarshaller

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  */
trait BaseCirceSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  /**
    * `Json` => HTTP entity
    *
    * @return marshaller for JSON value
    */
  implicit final def jsonMarshaller(
      implicit printer: Printer = Printer.noSpaces
  ): ToEntityMarshaller[Json] =
    Marshaller.withFixedContentType(`application/json`) { json =>
      HttpEntity(`application/json`, printer.pretty(json))
    }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit final def marshaller[A: Encoder](
      implicit printer: Printer = Printer.noSpaces
  ): ToEntityMarshaller[A] =
    jsonMarshaller(printer).compose(implicitly[Encoder[A]].apply)

  /**
    * HTTP entity => `Json`
    *
    * @return unmarshaller for `Json`
    */
  implicit final val jsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data             => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
      }

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A]
}

/**
  * Mix-in this trait to fail on the first error during unmarshalling.
  */
trait FailFastUnmarshaller { this: BaseCirceSupport =>

  override implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    def decode(json: Json) = implicitly[Decoder[A]].decodeJson(json).fold(throw _, identity)
    jsonUnmarshaller.map(decode)
  }
}

/**
  * Mix-in this trait to accumulate all errors during unmarshalling.
  */
trait ErrorAccumulatingUnmarshaller { this: BaseCirceSupport =>

  override implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    def decode(json: Json) =
      implicitly[Decoder[A]]
        .accumulating(json.hcursor)
        .fold(failures => throw ErrorAccumulatingCirceSupport.DecodingFailures(failures), identity)
    jsonUnmarshaller.map(decode)
  }
}
