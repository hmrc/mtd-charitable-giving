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

package v2.models.audit

import play.api.libs.json.{Json, OWrites}
import v2.models.domain.{GiftAidPayments, Gifts}

case class CharitableGivingAuditDetail(
                                        userType: String,
                                        agentReferenceNumber: String,
                                        nino: String,
                                        request: Option[CharitableGivingAuditRequest],
                                        `X-CorrelationId`: String,
                                        response: Option[AuditResponse] = None
                                      )

object CharitableGivingAuditDetail {
  implicit val writes: OWrites[CharitableGivingAuditDetail] = Json.writes[CharitableGivingAuditDetail]
}

case class CharitableGivingAuditRequest(giftAidPayments: Option[GiftAidPayments], gifts: Option[Gifts])

object CharitableGivingAuditRequest {
  implicit val writes: OWrites[CharitableGivingAuditRequest] = Json.writes[CharitableGivingAuditRequest]
}