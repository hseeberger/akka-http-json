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

package de.heikoseeberger.akkahttpupickle

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
object UpickleSupport extends UpickleSupport

/**
  * Automatic to and from JSON marshalling/unmarshalling using *upickle* protocol.
  */
trait UpickleSupport extends UpickleCustomizationSupport {

  override type Api = upickle.default.type

  override def api: Api = upickle.default
}
