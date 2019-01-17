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

import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures._
import v2.models.errors.MtdError
import v2.models.requestData.{AmendCharitableGivingRequest, AmendCharitableGivingRequestData, DesTaxYear}

class AmendCharitableGivingValidatorSpec extends UnitSpec {

  val validNino = "AA123456A"
  val validTaxYear = "2017-18"
  val validJsonBody = AnyContentAsJson(AmendCharitableGivingFixture.inputJson)

  private trait Test {
    val validator = new AmendCharitableGivingValidator()
  }

  "running a validation" should {

    "return no errors" when {
      "when the uri is valid and the JSON payload is Valid" in new Test {
//        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, validJsonBody)
//
//        val result: Seq[MtdError] = validator.validate(inputData)
//
//        result shouldBe List()
      }
    }

    "return a single error" when {

    }

    "return multiple errors" when {

    }
  }

}