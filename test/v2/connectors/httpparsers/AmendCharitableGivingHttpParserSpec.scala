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

package v2.connectors.httpparsers

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.models.errors._
import v2.models.outcomes.DesResponse


class AmendCharitableGivingHttpParserSpec extends UnitSpec {

  val method = "GET"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = Json.obj("transactionReference" -> transactionReference)
  val desResponse = DesResponse("X-123", transactionReference)

  "read" should {
    "return the transactionReference" when {
      "des returns a valid success response" in {
        val responseFromDes = HttpResponse(OK, Some(desExpectedJson), Map("CorrelationId" -> Seq("X-123")))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(method, url, responseFromDes)
        result shouldBe Right(desResponse)
      }
    }

    "return an INTERNAL_SERVER_ERROR" when {
      "the json returned can not be validated" in {
        val responseFromDes = HttpResponse(OK, Some(Json.obj("foo"->"bar")), Map("CorrelationId" -> Seq("X-123")))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(method, url, responseFromDes)
        result shouldBe Left(DownstreamError)
      }
    }

    def testDesErrorMap(desResponseStatus: Int, desCode: String, expectedMtdError: MtdError): Unit = {
      s"des returns an $desCode error" in {
        val json = Json.obj("code" -> desCode, "reason" -> "does not matter")
        val response = HttpResponse(desResponseStatus, Some(json))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(method, url, response)

        result shouldBe Left(expectedMtdError)
      }
    }

    "return the correct MTD error" when {
      testDesErrorMap(BAD_REQUEST, "INVALID_NINO", NinoFormatError)
      testDesErrorMap(BAD_REQUEST, "INVALID_TAXYEAR", TaxYearFormatError)
      testDesErrorMap(BAD_REQUEST, "INVALID_PAYLOAD", BadRequestError)
      testDesErrorMap(FORBIDDEN, "MISSING_GIFT_AID_AMOUNT", NonUKAmountNotSpecifiedRuleError)
      testDesErrorMap(FORBIDDEN, "MISSING_CHARITIES_NAME_GIFT_AID", NonUKNamesNotSpecifiedRuleError)
      testDesErrorMap(FORBIDDEN, "MISSING_CHARITIES_NAME_INVESTMENT", NonUKInvestmentsNamesNotSpecifiedRuleError)
      testDesErrorMap(FORBIDDEN, "MISSING_INVESTMENT_AMOUNT", NonUKInvestmentAmountNotSpecifiedRuleError)

      testDesErrorMap(BAD_REQUEST, "INVALID_TYPE", DownstreamError)
      testDesErrorMap(FORBIDDEN, "NOT_FOUND_INCOME_SOURCE", DownstreamError)
      testDesErrorMap(INTERNAL_SERVER_ERROR, "SERVER_ERROR", DownstreamError)
    }
  }
}
