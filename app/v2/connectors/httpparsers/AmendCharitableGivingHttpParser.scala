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

import play.api.http.Status.{BAD_REQUEST, OK, FORBIDDEN, INTERNAL_SERVER_ERROR}
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v2.models.errors._
import v2.models.outcomes.{AmendCharitableGivingConnectorOutcome, DesResponse}

object AmendCharitableGivingHttpParser extends HttpParser {

  private val jsonReads: Reads[String] = (__ \ "transactionReference").read[String]

  implicit val amendHttpReads: HttpReads[AmendCharitableGivingConnectorOutcome] = new HttpReads[AmendCharitableGivingConnectorOutcome] {
    override def read(method: String, url: String, response: HttpResponse): AmendCharitableGivingConnectorOutcome = {


      // scalastyle:off
      (response.status, response.jsonOpt) match {
        case (OK, _) => parseResponse(response)
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_NINO)) => Left(NinoFormatError)
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TAXYEAR)) => Left(TaxYearFormatError)
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_PAYLOAD)) => Left(BadRequestError)
        case (BAD_REQUEST, ErrorCode(DesErrorCode.INVALID_TYPE)) => Left(DownstreamError)
        case (FORBIDDEN, ErrorCode(DesErrorCode.MISSING_GIFT_AID_AMOUNT)) => Left(NonUKAmountNotSpecifiedRuleError)
        case (FORBIDDEN, ErrorCode(DesErrorCode.MISSING_CHARITIES_NAME_GIFT_AID)) => Left(NonUKNamesNotSpecifiedRuleError)
        case (FORBIDDEN, ErrorCode(DesErrorCode.MISSING_CHARITIES_NAME_INVESTMENT)) => Left(NonUKInvestmentsNamesNotSpecifiedRuleError)
        case (FORBIDDEN, ErrorCode(DesErrorCode.MISSING_INVESTMENT_AMOUNT)) => Left(NonUKInvestmentAmountNotSpecifiedRuleError)
        case (FORBIDDEN, ErrorCode(DesErrorCode.NOT_FOUND_INCOME_SOURCE)) => Left(DownstreamError)
        case (INTERNAL_SERVER_ERROR, _) => Left(DownstreamError)
      }
      // scalastyle:on
    }
    private def parseResponse(response: HttpResponse) = response.validateJson[String](jsonReads) match {
      case Some(ref) => Right(DesResponse(retrieveCorrelationId(response), ref))
      case None => Left(DownstreamError)
    }
  }
}
