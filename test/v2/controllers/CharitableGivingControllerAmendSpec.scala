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

package v2.controllers

import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.requestParsers.MockAmendCharitableGivingRequestDataParser
import v2.mocks.services.{MockCharitableGivingService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.{AmendCharitableGiving, GiftAidPayments, Gifts}
import v2.models.requestData.{AmendCharitableGivingRequest, AmendCharitableGivingRequestData, DesTaxYear}

import scala.concurrent.Future

class CharitableGivingControllerAmendSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCharitableGivingService
    with MockAmendCharitableGivingRequestDataParser {

    val hc = HeaderCarrier()

    val target =  new CharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      charitableGivingService = mockCharitableGivingService,
      amendCharitableGivingRequestDataParser = mockAmendCharitableGivingRequestDataParser
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val charitableGivingWithNonUKCharityDonations: String =
    """
      |{
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

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val amendCharitableGivingRequest: AmendCharitableGivingRequest = AmendCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear),
    AmendCharitableGiving(GiftAidPayments(None, None, None, None, None, None), Gifts(None, None, None, None)))

  "amend" should {
    "return a successful response with header X-CorrelationId" when {
      "the request received is valid" in new Test() {

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(Json.parse(charitableGivingWithNonUKCharityDonations))))
          .returns(Right(amendCharitableGivingRequest))

        MockCharitableGivingService.amend(amendCharitableGivingRequest)
          .returns(Future.successful(Right(correlationId)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(Json.parse(charitableGivingWithNonUKCharityDonations)))
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
  }
}
