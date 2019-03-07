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

package v2.services

import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.CharitableGivingFixture._
import v2.mocks.connectors.MockDesConnector
import v2.models.errors._
import v2.models.outcomes.DesResponse
import v2.models.requestData.{DesTaxYear, RetrieveCharitableGivingRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCharitableGivingServiceSpec extends ServiceSpec {

  trait Test extends MockDesConnector {
    lazy val target = new CharitableGivingService(connector)
  }


  val correlationId = "X-123"
  val nino = "AA123456A"
  val desTaxYear = "2018"
  val expectedDesResponse = DesResponse(correlationId, charitableGivingModel)
  val input = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear(desTaxYear))
  val output = DesResponse(correlationId, charitableGivingModel)

  "calling retrieve" should {
    "return a valid correlationId" when {
      "a valid data is passed" in new Test {
        MockedDesConnector.retrieve(input).returns(Future.successful(Right(expectedDesResponse)))
        private val result = await(target.retrieve(input))

        result shouldBe Right(output)
      }
    }
  }

  "return multiple errors" when {
    "the desConnector returns multiple errors" in new Test {

      val response = DesResponse(correlationId, MultipleErrors(Seq(
        Error("NOT_FOUND_PERIOD", "Doesn't matter"),
        Error("INVALID_TAXYEAR", "Doesn't matter"))))
      val expected = ErrorWrapper(Some(correlationId), BadRequestError, Some(Seq(NotFoundError, TaxYearFormatError)))

      MockedDesConnector.retrieve(input).returns(Future.successful(Left(response)))
      private val result = await(target.retrieve(input))
      result shouldBe Left(expected)
    }
  }

  "return a single error" when {
    "the desConnector returns multiple errors and one maps to a DownstreamError" in new Test {

      val response = DesResponse(correlationId, MultipleErrors(Seq(
        Error("NOT_FOUND_PERIOD", "Doesn't matter"),
        Error("INVALID_INCOME_SOURCE", "Doesn't matter"))))
      val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

      MockedDesConnector.retrieve(input).returns(Future.successful(Left(response)))
      private val result = await(target.retrieve(input))
      result shouldBe Left(expected)
    }
  }

  "return a generic error" in new Test {
    val response = DesResponse(correlationId, OutboundError(DownstreamError))
    val expected = ErrorWrapper(Some(correlationId), DownstreamError, None)

    MockedDesConnector.retrieve(input).returns(Future.successful(Left(response)))
    private val result = await(target.retrieve(input))
    result shouldBe Left(expected)

  }

  val errorMap: Map[String, Error] = Map(
    "INVALID_TYPE" -> DownstreamError,
    "INVALID_NINO" -> NinoFormatError,
    "INVALID_TAXYEAR" -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE" -> DownstreamError,
    "NOT_FOUND_PERIOD" -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE" -> DownstreamError,
    "SERVER_ERROR" -> DownstreamError,
    "SERVICE_UNAVAILABLE" -> DownstreamError
  )


  for (error <- errorMap.keys) {
    s"the DesConnector returns a single $error error" in new Test {
      val response = DesResponse(correlationId, SingleError(Error(error, "doesn't matter")))
      val expected = ErrorWrapper(Some(correlationId), errorMap(error), None)

      MockedDesConnector.retrieve(input).returns(Future.successful(Left(response)))

      private val result = await(target.retrieve(input))
      result shouldBe Left(expected)
    }

  }





}
