/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.controllers.requestParsers.validators.validations

import v2.models.errors.Error

object DependentDefinedValidation {

  def validate[A, B](firstValue: Option[A], secondValue: Option[B], error: Error): List[Error] = {

    (firstValue.isDefined, secondValue.isDefined) match {
      case (true, false) => List(error)
      case (true, true) | (false, false) | (false, true) => NoValidationErrors
    }

  }


}
