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

import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.mocks.validators.MockAmendCharitableGivingValidator
import v2.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v2.models.requestData.{AmendCharitableGivingRequest, AmendCharitableGivingRequestData, DesTaxYear, RetrieveCharitableGivingRequest}

class AmendCharitableGivingRequestDataParserSpec extends UnitSpec {

  val validNino = "AA123456A"
  val validTaxYear = "2017-18"
  val validJsonBody = AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)
  val correlationId = "X-123"

  trait Test extends MockAmendCharitableGivingValidator {
    lazy val parser = new AmendCharitableGivingRequestDataParser(mockValidator)
  }


  "parseRequest" should {

    "return an EopsDeclaration submission" when {
      "valid request data is supplied" in new Test {

        val amendCharitableGivingRequestData =
          AmendCharitableGivingRequestData(validNino, validTaxYear, validJsonBody)

        val amendCharitableGivingRequest =
          AmendCharitableGivingRequest(Nino(validNino), DesTaxYear(validTaxYear), CharitableGivingFixture.charitableGivingModel)

        MockedAmendCharitableGivingValidator.validate(amendCharitableGivingRequestData)
          .returns(List())

        parser.parseRequest(amendCharitableGivingRequestData) shouldBe Right(amendCharitableGivingRequest)
      }
    }

    "return an ErrorWrapper" when {

      val invalidNino = "foobar"
      val invalidDate = "bad-date"

      "a single validation error occurs" in new Test {
        val amendCharitableGivingRequestData =
          AmendCharitableGivingRequestData(invalidNino, validTaxYear, validJsonBody)

        val expectedResponse =
          ErrorWrapper(None, NinoFormatError, None)

        MockedAmendCharitableGivingValidator.validate(amendCharitableGivingRequestData)
          .returns(List(NinoFormatError))

        val receivedResponse = parser.parseRequest(amendCharitableGivingRequestData)
        expectedResponse.copy()
        receivedResponse shouldBe Left(expectedResponse)
      }

      "multiple validation errors occur" in new Test {
        val amendCharitableGivingRequestData =
          AmendCharitableGivingRequestData(validNino, invalidDate, validJsonBody)

        val multipleErrorWrapper =
          ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        MockedAmendCharitableGivingValidator.validate(amendCharitableGivingRequestData)
          .returns(List(NinoFormatError, TaxYearFormatError))


        parser.parseRequest(amendCharitableGivingRequestData) shouldBe Left(multipleErrorWrapper)
      }
    }

  }
}
