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

class BothFieldsDefinedValidationSpec extends UnitSpec with JsonErrorValidators {

  val dummyError = MtdError("DUMMY_ERROR", "For testing only")

  "validate" should {
    "return no errors" when {
      "when both fields are supplied" in {

        val firstValue = Some("FIRST")
        val secondValue = Some("SECOND")
        val validationResult = BothFieldsDefinedValidation.validate(firstValue, secondValue, dummyError)
        validationResult.isEmpty shouldBe true

      }

      "when both field are not supplied" in {
        val firstValue = None
        val secondValue = None
        val validationResult = BothFieldsDefinedValidation.validate(firstValue, secondValue, dummyError)
        validationResult.isEmpty shouldBe true
      }

      "when both fields are defined and are of different types" in {
        val firstValue = Some("STRING ARGUMENT")
        val secondValue = Some(List("LIST", "OF", "STRINGS"))
        val validationResult = BothFieldsDefinedValidation.validate(firstValue, secondValue, dummyError)
        validationResult.isEmpty shouldBe true
      }

    }

    "return the supplied error" when {
      "the first field is missing and the second field is supplied" in {
        val firstValue = None
        val secondValue = Some("SECOND")
        val validationResult = BothFieldsDefinedValidation.validate(firstValue, secondValue, dummyError)

        validationResult.isEmpty shouldBe false
        validationResult.size shouldBe 1
        validationResult.head shouldBe dummyError
      }

      "the second field is missing and the first field is supplied" in {
        val firstValue = Some("FIRST")
        val secondValue = None
        val validationResult = BothFieldsDefinedValidation.validate(firstValue, secondValue, dummyError)

        validationResult.isEmpty shouldBe false
        validationResult.size shouldBe 1
        validationResult.head shouldBe dummyError
      }

    }

  }
}
