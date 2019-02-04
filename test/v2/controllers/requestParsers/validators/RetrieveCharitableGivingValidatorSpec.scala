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

package v2.controllers.requestParsers.validators

import support.UnitSpec
import v2.models.errors.{NinoFormatError, TaxYearFormatError, TaxYearNotSpecifiedRuleError}
import v2.models.requestData.RetrieveCharitableGivingRawData

class RetrieveCharitableGivingValidatorSpec extends UnitSpec {

  val validNino = "AA123456A"
  val validTaxYear = "2017-18"

  private trait Test {
    val validator = new RetrieveCharitableGivingValidator()
  }

  "calling validate" should {
    "return no errors" when {
      "valid request data is supplied" in new Test {
        val inputData = RetrieveCharitableGivingRawData(validNino, validTaxYear)
        val result = validator.validate(inputData)
        result shouldBe List()
      }
    }

    "return a single error" when {
      "an invalid NINO is supplied" in new Test {
        val inputData = RetrieveCharitableGivingRawData("BAD_NINO_HERE", validTaxYear)
        val result = validator.validate(inputData)
        result.size shouldBe 1
        result.head shouldBe NinoFormatError
      }

      "an invalid tax year is supplied" in new Test {
        val inputData = RetrieveCharitableGivingRawData(validNino, "61725465142")
        val result = validator.validate(inputData)
        result.size shouldBe 1
        result.head shouldBe TaxYearFormatError
      }

      "a tax year below the minimum allowed year is supplied" in new Test {
        val inputData = RetrieveCharitableGivingRawData(validNino, "2014-15")
        val result = validator.validate(inputData)
        result.size shouldBe 1
        result.head shouldBe TaxYearNotSpecifiedRuleError
      }

    }

  }
}
