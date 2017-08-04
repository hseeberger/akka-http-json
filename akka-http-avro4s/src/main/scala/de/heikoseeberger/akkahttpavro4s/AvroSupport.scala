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

import java.io.{ ByteArrayOutputStream, IOException }

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.ContentTypeRange
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.util.ByteString
import com.sksamuel.avro4s._
import org.apache.commons.compress.utils.CharsetNames

import scala.collection.immutable.Seq
import scala.util.{ Failure, Success }

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
object AvroSupport extends AvroSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *avro4s* protocol.
  */
trait AvroSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  private val jsonStringMarshaller = Marshaller.stringMarshaller(`application/json`)

  /**
    * HTTP entity => `A`
    *
    * @tparam A type to decode
    * @return unmarshaller for `A`
    */
  implicit def unmarshaller[A: SchemaFor: FromRecord]: FromEntityUnmarshaller[A] = {
    def parse(s: String) = AvroInputStream.json[A](s.getBytes).singleEntity match {
      case Success(json)  => json
      case Failure(error) => sys.error(error.getMessage)
    }

    jsonStringUnmarshaller.map(parse)
  }

  /**
    * `A` => HTTP entity
    *
    * @tparam A type to encode
    * @return marshaller for any `A` value
    */
  implicit def marshaller[A: SchemaFor: ToRecord]: ToEntityMarshaller[A] = {
    def encode(data: A): String = {
      val baos = new ByteArrayOutputStream()
      try {
        val output = AvroOutputStream.json[A](baos)
        try {
          output.write(data)
        } finally {
          try output.close()
          catch {
            case _: IOException => // Ignoring closing exceptions
          }
        }
        baos.toString(CharsetNames.UTF_8)
      } finally {
        try baos.close()
        catch {
          case _: IOException => // Ignoring closing exceptions
        }
      }
    }

    jsonStringMarshaller.compose(encode)
  }
}
