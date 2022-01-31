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

package de.heikoseeberger.akkahttpziojson

object Zio2JsonSupport extends Zio2JsonSupport

/**
  * JSON marshalling/unmarshalling using zio-json codec implicits.
  *
  * The marshaller writes `A` to JSON `HTTPEntity`.
  *
  * The unmarshaller follows zio-json's early exit strategy, attempting to reading JSON to an `A`.
  *
  * A safe unmarshaller is provided to attempt reading JSON to an `Either[String, A]` instead.
  *
  * No intermediate JSON representation as per zio-json's design.
  */
trait Zio2JsonSupport extends ZioJsonSupport {}
