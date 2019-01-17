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

package v2.models

import play.api.libs.json.JsValue
import support.UnitSpec
import v2.fixtures.Fixtures.AmendCharitableGivingFixture._
import v2.models.utils.JsonErrorValidators

class AmendCharitableGivingSpec extends UnitSpec with JsonErrorValidators {


  "reads" should {

    "return an AmendCharitablGiving model" when {
      "correct json is supplied" in {
        val model = AmendCharitableGiving.reads.reads(inputJson).get
        model shouldBe amendCharitableGivingModel
      }
    }

    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/specifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/oneOffSpecifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/specifiedYearTreatedAsPreviousYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/followingYearTreatedAsSpecifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/nonUKCharities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/giftAidPayments/nonUKCharityNames",
      replacement = "notAnArray".toJson,
      expectedError = JsonError.JSARRAY_FORMAT_EXCEPTION
    )

    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/gifts/landAndBuildings",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/gifts/sharesOrSecurities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/gifts/investmentsNonUKCharities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[AmendCharitableGiving](inputJson)(
      path = "/gifts/investmentsNonUKCharityNames",
      replacement = "notAnArray".toJson,
      expectedError = JsonError.JSARRAY_FORMAT_EXCEPTION
    )
  }

  "writes" should {

    "create the DES formatted JSON" when {
      "a correct model is supplied" in {
        val json: JsValue = AmendCharitableGiving.writes.writes(amendCharitableGivingModel)
        json shouldBe outputJson
      }
    }
  }

}
