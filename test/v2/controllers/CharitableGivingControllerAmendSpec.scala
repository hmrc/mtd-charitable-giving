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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.mocks.requestParsers.{MockAmendCharitableGivingRequestDataParser, MockRetrieveCharitableGivingRequestDataParser}
import v2.mocks.services.{MockAmendCharitableGivingService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.errors._
import v2.models.requestData.{AmendCharitableGivingRequest, AmendCharitableGivingRequestData, DesTaxYear}
import v2.models.{CharitableGiving, GiftAidPayments, Gifts}

import scala.concurrent.Future

class CharitableGivingControllerAmendSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendCharitableGivingService
    with MockAmendCharitableGivingRequestDataParser
    with MockRetrieveCharitableGivingRequestDataParser {

    val hc = HeaderCarrier()

    val target = new CharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      charitableGivingService = mockAmendCharitableGivingService,
      amendCharitableGivingRequestDataParser = mockAmendCharitableGivingRequestDataParser,
      retrieveCharitableGivingRequestDataParser = mockRetrieveCharitableGivingRequestDataParser

    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val amendCharitableGivingRequest: AmendCharitableGivingRequest = AmendCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear),
    CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None))))

  "amend" should {
    "return a successful response with header X-CorrelationId" when {
      "the request received is valid" in new Test() {

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Right(amendCharitableGivingRequest))

        MockCharitableGivingService.amend(amendCharitableGivingRequest)
          .returns(Future.successful(Right(correlationId)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(CharitableGivingFixture.mtdFormatJson))
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return single error response with status 400" when {
      "the request received failed the validation" in new Test() {

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Left(ErrorWrapper(None, NinoFormatError, None)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(CharitableGivingFixture.mtdFormatJson))
        status(result) shouldBe BAD_REQUEST
        header("X-CorrelationId", result) nonEmpty
      }
    }

    "return a 400 Bad Request with a single error" when {

      val badRequestErrorsFromParser = List(
        BadRequestError,
        NinoFormatError,
        TaxYearFormatError,
        EmptyOrNonMatchingBodyRuleError,
        GiftAidAndGiftsEmptyRuleError,
        GiftAidSpecifiedYearFormatError,
        GiftAidOneOffSpecifiedYearFormatError,
        GiftAidSpecifiedYearPreviousFormatError,
        GiftAidFollowingYearSpecifiedFormatError,
        GiftAidNonUKCharityAmountFormatError,
        GiftAidNonUKNamesFormatError,
        GiftsSharesSecuritiesFormatError,
        GiftsLandsBuildingsFormatError,
        GiftsInvestmentsAmountFormatError,
        GiftsNonUKInvestmentsNamesFormatError,
        NonUKNamesNotSpecifiedRuleError,
        NonUKAmountNotSpecifiedRuleError,
        NonUKInvestmentsNamesNotSpecifiedRuleError,
        NonUKInvestmentAmountNotSpecifiedRuleError,
        TaxYearNotSpecifiedRuleError
      )

      val badRequestErrorsFromService = List(
        NinoFormatError,
        TaxYearFormatError,
        BadRequestError,
        NonUKNamesNotSpecifiedRuleError,
        NonUKAmountNotSpecifiedRuleError,
        NonUKInvestmentsNamesNotSpecifiedRuleError,
        NonUKInvestmentAmountNotSpecifiedRuleError
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

    "return a valid error response" when {
      "multiple errors exist" in new Test() {
        val amendCharitableGivingRequestData = AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson))
        val multipleErrorResponse = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Left(multipleErrorResponse))

        val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

        status(response) shouldBe BAD_REQUEST
        contentAsJson(response) shouldBe Json.toJson(multipleErrorResponse)
        header("X-CorrelationId", response) shouldBe Some(correlationId)
      }
    }

  }

  def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the parser" in new Test {

      val amendCharitableGivingRequestData = AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson))

      MockAmendCharitableGivingRequestDataParser.parseRequest(
        AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
        .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)
    }
  }

  def errorsFromServiceTester(error: MtdError, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the service" in new Test {

      val amendCharitableGivingRequestData = AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson))

      MockAmendCharitableGivingRequestDataParser.parseRequest(
        AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
        .returns(Right(amendCharitableGivingRequest))

      MockCharitableGivingService.amend(amendCharitableGivingRequest)
        .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), error, None))))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)
    }
  }

}
