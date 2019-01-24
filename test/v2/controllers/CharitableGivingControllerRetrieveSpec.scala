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

import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.mocks.requestParsers.{MockAmendCharitableGivingRequestDataParser, MockRetrieveCharitableGivingRequestDataParser}
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveCharitableGivingService}
import v2.models.requestData._
import v2.fixtures.Fixtures.CharitableGivingFixture.charitableGivingModel
import v2.models.CharitableGiving
import v2.models.errors.{ErrorWrapper, MtdError}
import v2.models.outcomes.{DesResponse, RetrieveCharitableGivingOutcome}

import scala.concurrent.Future

class CharitableGivingControllerRetrieveSpec extends ControllerBaseSpec {

  trait Test extends
    MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveCharitableGivingService
    with MockRetrieveCharitableGivingRequestDataParser
    with MockAmendCharitableGivingRequestDataParser {
    val hc = HeaderCarrier()

    val target = new CharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      charitableGivingService = mockRetrieveCharitableGivingService,
      amendCharitableGivingRequestDataParser = mockAmendCharitableGivingRequestDataParser,
      retrieveCharitableGivingRequestDataParser = mockRetrieveCharitableGivingRequestDataParser
    )
  }

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val retrieveCharitableGivingRequest: RetrieveCharitableGivingRequest = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear))
  val errorWrapper: ErrorWrapper = ErrorWrapper(None,MtdError("abc", "abc"),None)
  val charitableGiving = charitableGivingModel

  "retrieve" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test() {

        MockRetrieveCharitableGivingRequestDataParser.parseRequest(
          RetrieveCharitableGivingRequestData(nino, taxYear))
          .returns(Right(retrieveCharitableGivingRequest))

        MockCharitableGivingService.retrieve(retrieveCharitableGivingRequest)
          .returns(Future.successful(Right(DesResponse(correlationId, charitableGivingModel))))

        val result: Future[Result] = target.retrieve(nino, taxYear)(fakeGetRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

  }
}