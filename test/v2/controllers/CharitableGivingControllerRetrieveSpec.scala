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
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.fixtures.Fixtures.CharitableGivingFixture.charitableGivingModel
import v2.mocks.requestParsers.{MockAmendCharitableGivingRequestDataParser, MockRetrieveCharitableGivingRequestDataParser}
import v2.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveCharitableGivingService}
import v2.models.errors._
import v2.models.outcomes.DesResponse
import v2.models.requestData._

import scala.concurrent.Future

class CharitableGivingControllerRetrieveSpec extends ControllerBaseSpec {

  trait Test extends
    MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveCharitableGivingService
    with MockRetrieveCharitableGivingRequestDataParser
    with MockAmendCharitableGivingRequestDataParser
    with MockAuditService {
    val hc = HeaderCarrier()

    val controller = new CharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      charitableGivingService = mockRetrieveCharitableGivingService,
      amendCharitableGivingRequestDataParser = mockAmendCharitableGivingRequestDataParser,
      retrieveCharitableGivingRequestDataParser = mockRetrieveCharitableGivingRequestDataParser,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val retrieveCharitableGivingRequest: RetrieveCharitableGivingRequest = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))
  val errorWrapper: ErrorWrapper = ErrorWrapper(None, Error("abc", "abc"), None)

  "retrieve" should {
    "return a successful response with header X-CorrelationId and body" when {
      "the request received is valid" in new Test() {

        MockRetrieveCharitableGivingRequestDataParser.parseRequest(
          RetrieveCharitableGivingRawData(nino, taxYear))
          .returns(Right(retrieveCharitableGivingRequest))

        MockCharitableGivingService.retrieve(retrieveCharitableGivingRequest)
          .returns(Future.successful(Right(DesResponse(correlationId, charitableGivingModel))))

        val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)
        status(result) shouldBe OK
        contentAsJson(result) shouldBe CharitableGivingFixture.mtdFormatJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }

    "return single error response with status 400" when {
      "the request received failed the validation" in new Test() {

        MockRetrieveCharitableGivingRequestDataParser.parseRequest(
          RetrieveCharitableGivingRawData(nino, taxYear))
          .returns(Left(ErrorWrapper(None, NinoFormatError, None)))

        val result: Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)
        status(result) shouldBe BAD_REQUEST
        header("X-CorrelationId", result).nonEmpty shouldBe true
      }
    }

    "return a 400 Bad Request with a single error" when {

      val badRequestErrorsFromParser = List(
        NinoFormatError,
        TaxYearFormatError,
        TaxYearNotSpecifiedRuleError,
        RuleTaxYearRangeExceededError
      )

      val badRequestErrorsFromService = List(
        NinoFormatError,
        TaxYearFormatError
      )

      badRequestErrorsFromParser.foreach(errorsFromParserTester(_, BAD_REQUEST))
      badRequestErrorsFromService.foreach(errorsFromServiceTester(_, BAD_REQUEST))

    }

    "return a 500 Internal Server Error with a single error" when {

      val internalServerErrorErrors = List(
        DownstreamError
      )

      internalServerErrorErrors.foreach(errorsFromParserTester(_, INTERNAL_SERVER_ERROR))
      internalServerErrorErrors.foreach(errorsFromServiceTester(_, INTERNAL_SERVER_ERROR))

    }

    "return a 404 Not Found Error with a single error" when {

      val notFoundErrors = List(
        NotFoundError
      )

      notFoundErrors.foreach(errorsFromServiceTester(_, NOT_FOUND))

    }

  }

  def errorsFromParserTester(error: Error, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the parser" in new Test {

      val retrieveCharitableGivingRequestData = RetrieveCharitableGivingRawData(nino, taxYear)

      MockRetrieveCharitableGivingRequestDataParser.parseRequest(retrieveCharitableGivingRequestData)
        .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

      val response: Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)
    }
  }

  def errorsFromServiceTester(error: Error, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the service" in new Test {

      val retrieveCharitableGivingRequestData = RetrieveCharitableGivingRawData(nino, taxYear)
      val retrieveCharitableGivingRequest = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear.fromMtd(taxYear))

      MockRetrieveCharitableGivingRequestDataParser.parseRequest(retrieveCharitableGivingRequestData)
        .returns(Right(retrieveCharitableGivingRequest))

      MockCharitableGivingService.retrieve(retrieveCharitableGivingRequest)
        .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), error, None))))

      val response: Future[Result] = controller.retrieve(nino, taxYear)(fakeGetRequest)

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)
    }
  }

}
