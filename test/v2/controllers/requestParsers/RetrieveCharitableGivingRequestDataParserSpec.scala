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

package v2.controllers.requestParsers

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v2.mocks.validators.MockRetrieveCharitableGivingValidator
import v2.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v2.models.requestData.{DesTaxYear, RetrieveCharitableGivingRequest, RetrieveCharitableGivingRequestData}

class RetrieveCharitableGivingRequestDataParserSpec extends UnitSpec {

  class Test extends MockRetrieveCharitableGivingValidator {
    val parser = new RetrieveCharitableGivingRequestDataParser(mockRetrieveCharitableGivingValidator)
  }

  val validTaxYear = "2017-18"
  val invalidTaxYear = "2016-18"
  val invalidNino = "foobar"
  val validNino = "AA123456A"

  "calling parseRequest" should {
    "return a valid retrieve request" when {
      "a valid request data is supplied" in new Test() {
        val inputData = RetrieveCharitableGivingRequestData(validNino, validTaxYear)
        val expectedResult = RetrieveCharitableGivingRequest(Nino(validNino), DesTaxYear(validTaxYear))
        MockRetrieveCharitableGivingValidator.validate(inputData).returns(List())

        val result = parser.parseRequest(inputData)
        result shouldBe Right(expectedResult)
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        val retrieveCharitableGivingRequestData = RetrieveCharitableGivingRequestData(invalidNino, validTaxYear)
        val expectedResponse = ErrorWrapper(None, NinoFormatError, None)
        MockRetrieveCharitableGivingValidator.validate(retrieveCharitableGivingRequestData).returns(List(NinoFormatError))

        val result = parser.parseRequest(retrieveCharitableGivingRequestData)
        result shouldBe Left(expectedResponse)

      }

      "multiple validation errors occur" in new Test {
        val retrieveCharitableGivingRequestData = RetrieveCharitableGivingRequestData(invalidNino, invalidTaxYear)
        val expectedResponse = ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))
        MockRetrieveCharitableGivingValidator.validate(retrieveCharitableGivingRequestData).returns(List(NinoFormatError, TaxYearFormatError))


        val result = parser.parseRequest(retrieveCharitableGivingRequestData)
        result shouldBe Left(expectedResponse)
      }

    }

  }
}
