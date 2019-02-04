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
import v2.models.errors.Error
import v2.models.utils.JsonErrorValidators

class ArrayElementsRegexValidationSpec extends UnitSpec with JsonErrorValidators {

  val dummyError = Error("DUMMY_ERROR", "For testing only")
  val nonUkCharityNamesRegex: String = "^[^|]{1,75}$"


  "validate" should {
    "return no errors" when {
      "for all the array elements the regex is met " in {

        val arrayToValidate = Some(Seq("A", "B", "C"))
        val validationResult = ArrayElementsRegexValidation.validate(arrayToValidate, nonUkCharityNamesRegex, dummyError)
        validationResult.isEmpty shouldBe true

      }

      "the array is empty" in {
        val arrayToValidate = Some(Seq())
        val validationResult = ArrayElementsRegexValidation.validate(arrayToValidate, nonUkCharityNamesRegex, dummyError)
        validationResult.isEmpty shouldBe true
      }

      "the array field is not provided" in {
        val arrayToValidate = None
        val validationResult = ArrayElementsRegexValidation.validate(arrayToValidate, nonUkCharityNamesRegex, dummyError)
        validationResult.isEmpty shouldBe true
      }

    }

    "return the provided error" when {
      "at least one of the array elements does not match the regex" in {

        val arrayToValidate = Some(List("|||||||", "B", "C"))
        val validationResult = ArrayElementsRegexValidation.validate(arrayToValidate, nonUkCharityNamesRegex, dummyError)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe dummyError

      }
    }

  }
}
