/*
 * Copyright 2020 HM Revenue & Customs
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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.domain.{CharitableGiving, GiftAidPayments, Gifts}

object Fixtures {

  object CharitableGivingFixture {

    val mtdFormatJson: JsValue = Json.parse(
      s"""{
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

    val desFormatJson: JsValue = Json.parse(
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

    val charitableGivingModel: CharitableGiving = CharitableGiving(
      giftAidPayments = Some(
        GiftAidPayments(
          specifiedYear = Some(10000.00),
          oneOffSpecifiedYear = Some(1000.00),
          specifiedYearTreatedAsPreviousYear = Some(300.00),
          followingYearTreatedAsSpecifiedYear = Some(400.00),
          nonUKCharities = Some(2000.00),
          nonUKCharityNames = Some(Seq("International Charity A", "International Charity B"))
        )
      ),
      gifts = Some(
        Gifts(
          landAndBuildings = Some(700.00),
          sharesOrSecurities = Some(600.00),
          investmentsNonUKCharities = Some(300.00),
          investmentsNonUKCharityNames = Some(Seq("International Charity C", "International Charity D"))
        )
      )
    )
  }

}
