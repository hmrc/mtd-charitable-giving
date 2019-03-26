/*
 * Copyright 2019 HM Revenue & Customs
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

import support.UnitSpec
import v2.models.errors.MtdError
import v2.models.utils.JsonErrorValidators

class PredicateValidationSpec extends UnitSpec with JsonErrorValidators {

  val dummyError = MtdError("DUMMY_ERROR", "For testing only")

  "validate" should {
    "return no errors" when {
      "the supplied predicate is false" in {

        val predicateResult = false
        val validationResult = PredicateValidation.validate(predicateResult, dummyError)
        validationResult.isEmpty shouldBe true

      }
    }

    "return the provided error" when {
      "the supplied predicate is true" in {

        val predicateResult = true
        val validationResult = PredicateValidation.validate(predicateResult, dummyError)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe dummyError

      }
    }

  }
}
