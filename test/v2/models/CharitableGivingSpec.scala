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

package v2.models

import play.api.libs.json.JsValue
import support.UnitSpec
import v2.fixtures.Fixtures.CharitableGivingFixture._
import v2.models.domain.CharitableGiving
import v2.models.utils.JsonErrorValidators

class CharitableGivingSpec extends UnitSpec with JsonErrorValidators {


  "reads" should {

    "return an AmendCharitablGiving model" when {
      "correct json is supplied" in {
        val model = CharitableGiving.reads.reads(mtdFormatJson).get
        model shouldBe charitableGivingModel
      }
    }

    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/specifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/oneOffSpecifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/specifiedYearTreatedAsPreviousYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/followingYearTreatedAsSpecifiedYear",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/nonUKCharities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/giftAidPayments/nonUKCharityNames",
      replacement = "notAnArray".toJson,
      expectedError = JsonError.JSARRAY_FORMAT_EXCEPTION
    )

    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/gifts/landAndBuildings",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/gifts/sharesOrSecurities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/gifts/investmentsNonUKCharities",
      replacement = "notANumber".toJson,
      expectedError = JsonError.NUMBER_FORMAT_EXCEPTION
    )
    testPropertyType[CharitableGiving](mtdFormatJson)(
      path = "/gifts/investmentsNonUKCharityNames",
      replacement = "notAnArray".toJson,
      expectedError = JsonError.JSARRAY_FORMAT_EXCEPTION
    )
  }

  "writes" should {

    "create the DES formatted JSON" when {
      "a correct model is supplied" in {
        val json: JsValue = CharitableGiving.writes.writes(charitableGivingModel)
        json shouldBe desFormatJson
      }
    }
  }

  "desReads" should {

    "return an CharitableGiving model" when {
      "DES returns a valid json" in {
        val model = CharitableGiving.desReads.reads(desFormatJson).get
        model shouldBe charitableGivingModel
      }
    }
  }

  "desToMtdWrites" should {

    "generate a valid JSON" when {
      "a valid model is retrieved" in {
        val json: JsValue = CharitableGiving.desToMtdWrites.writes(charitableGivingModel)
        json shouldBe mtdFormatJson
      }
    }
  }

}
