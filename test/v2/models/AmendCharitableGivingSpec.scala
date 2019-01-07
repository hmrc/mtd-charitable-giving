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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.inbound.{AmendCharitableGiving, GiftAidPayments, Gifts}
import v2.models.utils.JsonErrorValidators

class AmendCharitableGivingSpec extends UnitSpec with JsonErrorValidators {


  val inputJson: JsValue = Json.parse(s"""{
      |  "giftAidPayments": {
      |    "specifiedYear": 10000.00,
      |    "oneOffSpecifiedYear": 1000.00,
      |    "specifiedYearTreatedAsPreviousYear": 300.00,
      |    "followingYearTreatedAsSpecifiedYear": 400.00,
      |    "nonUKCharities": 2000.00,
      |    "nonUKCharityNames": ["International Charity A","International Charity B"]
      |  },
      |  "gifts": {
      |    "landAndBuildings": 700.00,
      |    "sharesOrSecurities": 600.00,
      |    "investmentsNonUKCharities": 300.00,
      |    "investmentsNonUKCharityNames": ["International Charity C","International Charity D"]
      |  }
      |}""".stripMargin
  )

  val outputJson: JsValue = Json.parse(
    s"""{
       |  "giftAidPayments": {
       |    "currentYear": 10000.00,
       |    "oneOffCurrentYear": 1000.00,
       |    "currentYearTreatedAsPreviousYear": 300.00,
       |    "nextYearTreatedAsCurrentYear": 400.00,
       |    "nonUkCharities": 2000.00,
       |    "nonUkCharitiesCharityNames": ["International Charity A","International Charity B"]
       |  },
       |  "gifts": {
       |    "landAndBuildings": 700.00,
       |    "sharesOrSecurities": 600.00,
       |    "investmentsNonUkCharities": 300.00,
       |    "investmentsNonUkCharitiesCharityNames": ["International Charity C","International Charity D"]
       |  }
       |}""".stripMargin
  )

  val amendCharitableGivingModel: AmendCharitableGiving = AmendCharitableGiving(
    giftAidPayments = GiftAidPayments(
      specifiedYear = Some(10000.00),
      oneOffSpecifiedYear = Some(1000.00),
      specifiedYearTreatedAsPreviousYear = Some(300.00),
      followingYearTreatedAsSpecifiedYear = Some(400.00),
      nonUKCharities = Some(2000.00),
      nonUKCharityNames = Some(Seq("International Charity A", "International Charity B"))
    ),
    gifts = Gifts(
      landAndBuildings = Some(700.00),
      sharesOrSecurities = Some(600.00),
      investmentsNonUKCharities = Some(300.00),
      investmentsNonUKCharityNames = Some(Seq("International Charity C","International Charity D"))
    )
  )

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
