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

package v2.models.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

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
    ) (unlift(GiftAidPayments.unapply))

  val desReads: Reads[GiftAidPayments] = (
    (JsPath \ "currentYear").readNullable[BigDecimal] and
      (JsPath \ "oneOffCurrentYear").readNullable[BigDecimal] and
      (JsPath \ "currentYearTreatedAsPreviousYear").readNullable[BigDecimal] and
      (JsPath \ "nextYearTreatedAsCurrentYear").readNullable[BigDecimal] and
      (JsPath \ "nonUkCharities").readNullable[BigDecimal] and
      (JsPath \ "nonUkCharitiesCharityNames").readNullable[Seq[String]]
    ) (GiftAidPayments.apply _)

  val desToMtdWrites: Writes[GiftAidPayments] = Json.writes[GiftAidPayments]

}
