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

package v2.models.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CharitableGiving(giftAidPayments: Option[GiftAidPayments], gifts:Option[Gifts])

object CharitableGiving {

  implicit val reads: Reads[CharitableGiving] = Json.reads[CharitableGiving]

  implicit val writes: Writes[CharitableGiving] = Json.writes[CharitableGiving]

  val desReads: Reads[CharitableGiving] = (
    (__ \ "giftAidPayments").readNullable[GiftAidPayments](GiftAidPayments.desReads) and
      (__ \ "gifts").readNullable[Gifts](Gifts.desReads)
    ) (CharitableGiving.apply _)

  val desToMtdWrites: Writes[CharitableGiving] = (
    (__ \ "giftAidPayments").writeNullable[GiftAidPayments](GiftAidPayments.desToMtdWrites) and
      (__ \ "gifts").writeNullable[Gifts](Gifts.desToMtdWrites)
    ) (unlift(CharitableGiving.unapply))
}