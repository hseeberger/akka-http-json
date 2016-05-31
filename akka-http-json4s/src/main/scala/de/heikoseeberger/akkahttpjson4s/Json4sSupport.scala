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

import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import org.json4s.{ Formats, MappingException, Serialization }

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope *Json4s* protocol.
 *
 * Pretty printing is enabled if an implicit [[Json4sSupport.ShouldWritePretty.True]] is in scope.
 */
object Json4sSupport extends Json4sSupport {

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

  /**
   * HTTP entity => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def json4sUnmarshaller[A: Manifest](implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset { (data, charset) =>
        try serialization.read(data.decodeString(charset.nioCharset.name))
        catch {
          case MappingException("unknown error", ite: InvocationTargetException) => throw ite.getCause
        }
      }

  /**
   * `A` => HTTP entity
   *
   * @tparam A type to encode, must be upper bounded by `AnyRef`
   * @return marshaller for any `A` value
   */
  implicit def json4sMarshaller[A <: AnyRef](implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    shouldWritePretty match {
      case ShouldWritePretty.False => Marshaller.StringMarshaller.wrap(`application/json`)(serialization.write[A])
      case _                       => Marshaller.StringMarshaller.wrap(`application/json`)(serialization.writePretty[A])
    }
}
