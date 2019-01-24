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
import v2.models.requestData.{DesTaxYear, RetrieveCharitableGivingRequest, RetrieveCharitableGivingRequestData}

class RetrieveCharitableGivingRequestDataParserSpec extends UnitSpec{

  class Test extends MockRetrieveCharitableGivingValidator{
    val target = new RetrieveCharitableGivingRequestDataParser(mockRetrieveCharitableGivingValidator)
  }

  "calling parseRequest" should {
    "return a valid retrieve request" when {
      "a valid request data is supplied" in new Test() {
        val nino = "AA123456A"
        val taxYear = "2017-18"
        val inputData = RetrieveCharitableGivingRequestData(nino, taxYear)
        val expectedResult = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear))
        MockRetrieveCharitableGivingValidator.validate(inputData).returns(List())

        val result = target.parseRequest(inputData)
        result shouldBe Right(expectedResult)
      }
    }
  }
}