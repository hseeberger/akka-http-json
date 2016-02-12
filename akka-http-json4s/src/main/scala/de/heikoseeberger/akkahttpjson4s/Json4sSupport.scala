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

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import de.heikoseeberger.akkahttpjson4s.Json4sSupport.ShouldWritePretty
import org.json4s.{ Formats, Serialization }
import scala.concurrent.Future

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
 *
 * Pretty printing is enabled if an implicit [[Json4sSupport.ShouldWritePretty.True]] is in scope.
 */
object Json4sSupport extends Json4sSupport with Json4sSupportProtection {

  sealed abstract class ShouldWritePretty

  object ShouldWritePretty {
    object True extends ShouldWritePretty
    object False extends ShouldWritePretty
  }
}

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
 *
 * Pretty printing is enabled if an implicit [[Json4sSupport.ShouldWritePretty.True]] is in scope.
 */
trait Json4sSupport {
  import Json4sSupport._

  implicit def json4sUnmarshallerConverter[A: Manifest](serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    json4sUnmarshaller(manifest, serialization, formats)

  implicit def json4sUnmarshaller[A: Manifest](implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        serialization.read(input)
      }

  implicit def json4sMarshallerConverter[A <: AnyRef](serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    json4sMarshaller(serialization, formats, shouldWritePretty)

  implicit def json4sMarshaller[A <: AnyRef](implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    shouldWritePretty match {
      case ShouldWritePretty.False => Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(serialization.write[A])
      case _                       => Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`)(serialization.writePretty[A])
    }
}

/**
 * Protection against automatic to and from JSON marshalling/unmarshalling of types that we know have no sensible JSON representation.
 *
 * This makes sure users get the expected compile errors for these types, rather than unwanted marshalling.
 *
 * Because these implicits require many parameters,
 * it is unlikely that they will collide with user-defined marshallers for these types.
 */
trait Json4sSupportProtection {

  implicit def noJson4sUnmarshallerConverterForFuturesDueToAmbiguityA[A <: Future[_]: Manifest](serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] = ???
  implicit def noJson4sUnmarshallerConverterForFuturesDueToAmbiguityB[A <: Future[_]: Manifest](serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] = ???

  implicit def noJson4sUnmarshallerForFuturesDueToAmbiguityA[A <: Future[_]: Manifest](implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] = ???
  implicit def noJson4sUnmarshallerForFuturesDueToAmbiguityB[A <: Future[_]: Manifest](implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] = ???

  implicit def noJson4sMarshallerConverterForFuturesDueToAmbiguityA[A <: Future[_]](serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] = ???
  implicit def noJson4sMarshallerConverterForFuturesDueToAmbiguityB[A <: Future[_]](serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] = ???

  implicit def noJson4sMarshallerForFuturesDueToAmbiguityA[A <: Future[_]](implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] = ???
  implicit def noJson4sMarshallerForFuturesDueToAmbiguityB[A <: Future[_]](implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] = ???

}
