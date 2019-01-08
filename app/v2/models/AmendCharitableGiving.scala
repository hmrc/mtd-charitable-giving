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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class AmendCharitableGiving(giftAidPayments: GiftAidPayments, gifts: Gifts)


object AmendCharitableGiving {
  implicit val reads: Reads[AmendCharitableGiving] = Json.reads[AmendCharitableGiving]

  implicit val writes: Writes[AmendCharitableGiving] = Json.writes[AmendCharitableGiving]
}

case class GiftAidPayments(specifiedYear: Option[BigDecimal],
                           oneOffSpecifiedYear: Option[BigDecimal],
                           specifiedYearTreatedAsPreviousYear: Option[BigDecimal],
                           followingYearTreatedAsSpecifiedYear: Option[BigDecimal],
                           nonUKCharities: Option[BigDecimal],
                           nonUKCharityNames: Option[Seq[String]]
                          )

object GiftAidPayments {
  implicit val reads: Reads[GiftAidPayments] = Json.reads[GiftAidPayments]

  implicit val writes: Writes[GiftAidPayments] = (
      (JsPath \ "currentYear").writeNullable[BigDecimal] and
        (JsPath \ "oneOffCurrentYear").writeNullable[BigDecimal] and
        (JsPath \ "currentYearTreatedAsPreviousYear").writeNullable[BigDecimal] and
        (JsPath \ "nextYearTreatedAsCurrentYear").writeNullable[BigDecimal] and
        (JsPath \ "nonUkCharities").writeNullable[BigDecimal] and
        (JsPath \ "nonUkCharitiesCharityNames").writeNullable[Seq[String]]
    )(unlift(GiftAidPayments.unapply))
}

case class Gifts(landAndBuildings: Option[BigDecimal],
                 sharesOrSecurities: Option[BigDecimal],
                 investmentsNonUKCharities: Option[BigDecimal],
                 investmentsNonUKCharityNames: Option[Seq[String]])

object Gifts {
  implicit val reads: Reads[Gifts] = Json.reads[Gifts]

  implicit val write: Writes[Gifts] = (
    (JsPath \ "landAndBuildings").writeNullable[BigDecimal] and
      (JsPath \ "sharesOrSecurities").writeNullable[BigDecimal] and
      (JsPath \ "investmentsNonUkCharities").writeNullable[BigDecimal] and
      (JsPath \ "investmentsNonUkCharitiesCharityNames").writeNullable[Seq[String]]
  )(unlift(Gifts.unapply))
}


