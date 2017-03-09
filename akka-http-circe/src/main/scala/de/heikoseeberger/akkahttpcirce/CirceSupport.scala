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
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import io.circe.{ jawn, Decoder, Errors, Json, Printer, RootEncoder }

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
trait FailFastCirceSupport extends BaseCirceSupport with NoSpacesPrinter with FailFastUnmarshaller

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller accumulates all errors in the exception `Errors`.
  *
  * To use automatic codec derivation, user needs to import `io.circe.generic.auto._`.
  */
object ErrorAccumulatingCirceSupport extends ErrorAccumulatingCirceSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  * The unmarshaller accumulates all errors in the exception `Errors`.
  *
  * To use automatic codec derivation import `io.circe.generic.auto._`.
  */
trait ErrorAccumulatingCirceSupport
    extends BaseCirceSupport
    with NoSpacesPrinter
    with ErrorAccumulatingUnmarshaller

@deprecated(message = "Use either FailFastCirceSupport or ErrorAccumulatingCirceSupport",
            since = "1.13.0")
object CirceSupport extends FailFastCirceSupport

@deprecated(message = "Use either FailFastCirceSupport or ErrorAccumulatingCirceSupport",
            since = "1.13.0")
trait CirceSupport extends FailFastCirceSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
  */
trait BaseCirceSupport {

  /**
    * Printer used in the JSON marshaller.
    */
  def printer: Printer

  /**
    * `Json` => HTTP entity
    *
    * @return marshaller for JSON value
    */
  implicit final val jsonMarshaller: ToEntityMarshaller[Json] =
    Marshaller.withFixedContentType(`application/json`) { json =>
      HttpEntity(`application/json`, printer.pretty(json))
    }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit final def marshaller[A: RootEncoder]: ToEntityMarshaller[A] =
    jsonMarshaller.compose(implicitly[RootEncoder[A]].apply)

  /**
    * HTTP entity => `Json`
    *
    * @return unmarshaller for `Json`
    */
  implicit final val jsonUnmarshaller: FromEntityUnmarshaller[Json] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(`application/json`)
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
        .fold(decodingFailure => throw Errors(decodingFailure), identity)
    jsonUnmarshaller.map(decode)
  }
}

/**
  * Mix-in this trait to use a compact JSON printer during marshalling.
  */
trait NoSpacesPrinter { this: BaseCirceSupport =>

  override final def printer: Printer =
    Printer.noSpaces
}
