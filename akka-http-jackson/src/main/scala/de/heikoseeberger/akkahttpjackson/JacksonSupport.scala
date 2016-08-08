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

package de.heikoseeberger.akkahttpjackson

import akka.http.javadsl.marshallers.jackson.Jackson
import com.fasterxml.jackson.databind.ObjectMapper
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.unmarshalling.{ Unmarshaller, _ }
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect._

/**
 * Automatic to and from JSON marshalling/unmarshalling usung an in-scope Jackon's ObjectMapper
 */
object JacksonSupport extends JacksonSupport {
  val defaultObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
}

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {
  import JacksonSupport._

  /**
   * HTTP entity => `A`
   *
   * @param objectMapper
   * @tparam A
   * @return
   */
  implicit def jacksonUnmarshaller[A](
    implicit
    ct: ClassTag[A],
    objectMapper: ObjectMapper = defaultObjectMapper
  ): FromEntityUnmarshaller[A] = {
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset((data, charset) => {
        val x: A = objectMapper.readValue(
          data.decodeString(charset.nioCharset.name), ct.runtimeClass
        ).asInstanceOf[A]
        x
      })
  }

  /**
   * `A` => HTTP entity
   *
   * @param objectMapper
   * @tparam Object
   * @return
   */
  implicit def jacksonToEntityMarshaller[Object](
    implicit
    objectMapper: ObjectMapper = defaultObjectMapper
  ): ToEntityMarshaller[Object] = {
    Jackson.marshaller[Object](objectMapper)
  }
}
