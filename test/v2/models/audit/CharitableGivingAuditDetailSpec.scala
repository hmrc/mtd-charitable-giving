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

import play.api.http.Status
import play.api.libs.json.Json
import support.UnitSpec
import v2.models.domain.{GiftAidPayments, Gifts}

class CharitableGivingAuditDetailSpec extends UnitSpec {
  private val userType = "Organisation"
  private val agentReferenceNumber = "012345678"
  private val nino = "AA123456A"
  private val giftAidPayments = GiftAidPayments(
    Some(10000.50), Some(1000.00), Some(300.00), Some(400.00), Some(2000.00), Some(Seq("International Charity A", "International Charity B"))
  )
  private val gifts = Gifts(Some(700.00), Some(600.99), Some(300.00), Some(Seq("International Charity C", "International Charity D")))
  private val `X-CorrelationId` = "X-123"
  private val response = AuditResponse(Status.BAD_REQUEST, Seq(AuditError("FORMAT_NINO")))
  "writes" when {
    "passed a charitable giving audit model with all fields provided" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |  "userType": "Organisation",
             |  "agentReferenceNumber": "012345678",
             |  "nino": "AA123456A",
             |  "request": {
             |    "giftAidPayments": ${Json.toJson(giftAidPayments)},
             |    "gifts": ${Json.toJson(gifts)}
             |  },
             |  "X-CorrelationId": "X-123",
             |  "response": ${Json.toJson(response)}
             |}
           """.stripMargin)

        val request = CharitableGivingAuditRequest(Some(giftAidPayments), Some(gifts))

        val model = CharitableGivingAuditDetail(userType, agentReferenceNumber, nino, Some(request), `X-CorrelationId`, Some(response))

        Json.toJson(model) shouldBe json
      }
    }

    "passed a charitable giving audit model with only mandatory fields provided" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |  "userType": "Organisation",
             |  "agentReferenceNumber": "012345678",
             |  "nino": "AA123456A",
             |  "X-CorrelationId": "X-123"
             |}
           """.stripMargin)

        val model = CharitableGivingAuditDetail(userType, agentReferenceNumber, nino, None, `X-CorrelationId`, None)

        Json.toJson(model) shouldBe json
      }
    }
  }
}
