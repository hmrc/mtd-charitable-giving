/*
 * Copyright 2020 HM Revenue & Customs
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

import org.scalatest.OneInstancePerTest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.mocks.requestParsers.MockAmendCharitableGivingRequestDataParser
import v2.mocks.services.{MockAmendCharitableGivingService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, CharitableGivingAuditDetail}
import v2.models.domain.{CharitableGiving, GiftAidPayments, Gifts}
import v2.models.errors._
import v2.models.outcomes.DesResponse
import v2.models.requestData.{AmendCharitableGivingRawData, AmendCharitableGivingRequest, DesTaxYear}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendCharitableGivingControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockAmendCharitableGivingService
  with MockAmendCharitableGivingRequestDataParser
  with MockAuditService
  with OneInstancePerTest {

  trait Test {

    val hc = HeaderCarrier()

    val target = new AmendCharitableGivingController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      charitableGivingService = mockAmendCharitableGivingService,
      requestDataParser = mockAmendCharitableGivingRequestDataParser,
      auditService = mockAuditService,
      cc = cc
    )
    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val nino = "AA123456A"
  val taxYear = "2017-18"
  val correlationId = "X-123"
  val amendCharitableGivingRequest: AmendCharitableGivingRequest = AmendCharitableGivingRequest(Nino(nino), DesTaxYear.fromMtd(taxYear),
    CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None))))

  "amend" should {
    "return a successful response with header X-CorrelationId" when {
      "the request received is valid" in new Test() {

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Right(amendCharitableGivingRequest))

        MockCharitableGivingService.amend(amendCharitableGivingRequest)
          .returns(Future.successful(Right(DesResponse(correlationId,""))))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(CharitableGivingFixture.mtdFormatJson))
        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val detail = CharitableGivingAuditDetail("Individual", None, nino, taxYear, CharitableGivingFixture.mtdFormatJson, "X-123", None)
        val event: AuditEvent[CharitableGivingAuditDetail] = AuditEvent[CharitableGivingAuditDetail]("amendCharitableGivingTaxRelief",
          "update-charitable-giving-annual-summary", detail)
        MockedAuditService.verifyAuditEvent(event).once

      }
    }

    "return a 404 Not Found Error with a single error" when {

      val notFoundErrors = List(
        NotFoundError
      )

      notFoundErrors.foreach(errorsFromServiceTester(_, NOT_FOUND))
    }

    "return single error response with status 400" when {
      "the request received failed the validation" in new Test() {

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Left(ErrorWrapper(None, NinoFormatError, None)))

        val result: Future[Result] = target.amend(nino, taxYear)(fakePostRequest(CharitableGivingFixture.mtdFormatJson))
        status(result) shouldBe BAD_REQUEST
        header("X-CorrelationId", result).nonEmpty shouldBe true

        val detail = CharitableGivingAuditDetail("Individual", None, nino, taxYear, CharitableGivingFixture.mtdFormatJson, "X-123",
          Some(AuditResponse(BAD_REQUEST, Seq(AuditError(NinoFormatError.code)))))
        val event: AuditEvent[CharitableGivingAuditDetail] = AuditEvent[CharitableGivingAuditDetail]("amendCharitableGivingTaxRelief",
          "update-charitable-giving-annual-summary", detail)

        MockedAuditService.verifyAuditEvent(event.copy(detail = detail.copy(`X-CorrelationId` = header("X-CorrelationId", result).get))).once
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
        TaxYearNotSupportedRuleError,
        RuleTaxYearRangeExceededError
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
        val multipleErrorResponse = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError)))

        MockAmendCharitableGivingRequestDataParser.parseRequest(
          AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
          .returns(Left(multipleErrorResponse))

        val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

        status(response) shouldBe BAD_REQUEST
        contentAsJson(response) shouldBe Json.toJson(multipleErrorResponse)
        header("X-CorrelationId", response) shouldBe Some(correlationId)

        val detail = CharitableGivingAuditDetail("Individual", None, nino, taxYear, CharitableGivingFixture.mtdFormatJson, "X-123",
          Some(AuditResponse(BAD_REQUEST, Seq(AuditError(NinoFormatError.code), AuditError(TaxYearFormatError.code)))))
        val event: AuditEvent[CharitableGivingAuditDetail] = AuditEvent[CharitableGivingAuditDetail]("amendCharitableGivingTaxRelief",
          "update-charitable-giving-annual-summary", detail)
        MockedAuditService.verifyAuditEvent(event).once
      }
    }

  }

  def errorsFromParserTester(error: Error, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the parser" in new Test {
      MockAmendCharitableGivingRequestDataParser.parseRequest(
        AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
        .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)

      val detail = CharitableGivingAuditDetail("Individual", None, nino, taxYear, CharitableGivingFixture.mtdFormatJson, "X-123",
        Some(AuditResponse(expectedStatus, Seq(AuditError(error.code)))))
      val event: AuditEvent[CharitableGivingAuditDetail] = AuditEvent[CharitableGivingAuditDetail]("amendCharitableGivingTaxRelief",
        "update-charitable-giving-annual-summary", detail)
      MockedAuditService.verifyAuditEvent(event).once
    }
  }

  def errorsFromServiceTester(error: Error, expectedStatus: Int): Unit = {
    s"a ${error.code} error is returned from the service" in new Test {
      MockAmendCharitableGivingRequestDataParser.parseRequest(
        AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)))
        .returns(Right(amendCharitableGivingRequest))

      MockCharitableGivingService.amend(amendCharitableGivingRequest)
        .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), error, None))))

      val response: Future[Result] = target.amend(nino, taxYear)(fakePostRequest[JsValue](CharitableGivingFixture.mtdFormatJson))

      status(response) shouldBe expectedStatus
      contentAsJson(response) shouldBe Json.toJson(error)
      header("X-CorrelationId", response) shouldBe Some(correlationId)

      val detail = CharitableGivingAuditDetail("Individual", None, nino, taxYear, CharitableGivingFixture.mtdFormatJson, "X-123",
        Some(AuditResponse(expectedStatus, Seq(AuditError(error.code)))))
      val event: AuditEvent[CharitableGivingAuditDetail] = AuditEvent[CharitableGivingAuditDetail]("amendCharitableGivingTaxRelief",
        "update-charitable-giving-annual-summary", detail)
      MockedAuditService.verifyAuditEvent(event).once
    }
  }

}
