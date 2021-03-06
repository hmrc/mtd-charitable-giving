/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.connectors

import v2.models.domain.Nino
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.mocks.{MockAppConfig, MockHttpClient}
import v2.models.domain.{CharitableGiving, GiftAidPayments, Gifts}
import v2.models.errors.{MultipleErrors, NinoFormatError, SingleError, TaxYearFormatError}
import v2.models.outcomes.DesResponse
import v2.models.requestData.{AmendCharitableGivingRequest, DesTaxYear, RetrieveCharitableGivingRequest}

import scala.concurrent.Future

class DesConnectorSpec extends ConnectorSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new DesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
    MockedAppConfig.desBaseUrl returns url
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
    MockedAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "Amend charitable giving tax relief" should {
    "return a successful response with transactionId and correlationId" when {
      "a valid request is supplied" in new Test() {

        val expectedRef = "000000000001013"
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("2018")

        val expectedDesResponse = DesResponse("X-123", expectedRef)

        MockedHttpClient.post[CharitableGiving, AmendCharitableGivingConnectorOutcome](
          s"$url" + s"/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig,
          CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None))))
          .returns(Future.successful(Right(expectedDesResponse)))

        val result = await(connector.amend(AmendCharitableGivingRequest(Nino(nino), desTaxYear,
          CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None))))))

        result shouldBe Right(expectedDesResponse)
      }
    }

    "return an error response with correlationId" when {
      "an request supplied with invalid tax year" in new Test() {

        val expectedDesResponse = DesResponse("X-123", SingleError(TaxYearFormatError))
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("1234")
        val charitableGiving = CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None)))

        MockedHttpClient.post[CharitableGiving, AmendCharitableGivingConnectorOutcome](
          s"$url/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig,
          charitableGiving)
          .returns(Future.successful(Left(expectedDesResponse)))

        val result = await(connector.amend(AmendCharitableGivingRequest(Nino(nino), desTaxYear,
          charitableGiving)))

        result shouldBe Left(expectedDesResponse)
      }
    }

    "return a response with multiple errors and correlationId" when {
      "an request supplied with invalid tax year and invalid Nino " in new Test() {

        val expectedDesResponse = DesResponse("X-123", MultipleErrors(Seq(TaxYearFormatError, NinoFormatError)))
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("1234")
        val charitableGiving = CharitableGiving(Some(GiftAidPayments(None, None, None, None, None, None)), Some(Gifts(None, None, None, None)))

        MockedHttpClient.post[CharitableGiving, AmendCharitableGivingConnectorOutcome](
          s"$url/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig,
          charitableGiving)
          .returns(Future.successful(Left(expectedDesResponse)))

        val result = await(connector.amend(AmendCharitableGivingRequest(Nino(nino), desTaxYear,
          charitableGiving)))

        result shouldBe Left(expectedDesResponse)
      }
    }
  }

  "Retrieve charitable giving tax relief" should {
    "return a valid charitable giving json" when {
      "a valid request is supplied" in new Test() {
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("2018")
        val httpParsedDesResponse = DesResponse("X-123", CharitableGivingFixture.charitableGivingModel)

        MockedHttpClient.get[RetrieveCharitableGivingConnectorOutcome](
          s"$url" + s"/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig)
          .returns(Future.successful(Right(httpParsedDesResponse)))

        val result = await(connector.retrieve(RetrieveCharitableGivingRequest(Nino(nino), desTaxYear)))
        result shouldBe Right(httpParsedDesResponse)
      }
    }

    "return an error response with correlationId" when {
      "an request supplied with invalid tax year" in new Test() {

        val expectedDesResponse = DesResponse("X-123", SingleError(TaxYearFormatError))
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("1234")

        MockedHttpClient.get[RetrieveCharitableGivingConnectorOutcome](
          s"$url/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig)
          .returns(Future.successful(Left(expectedDesResponse)))

        val result = await(connector.retrieve(RetrieveCharitableGivingRequest(Nino(nino), desTaxYear)))

        result shouldBe Left(expectedDesResponse)
      }
    }

    "return a response with multiple errors and correlationId" when {
      "an request supplied with invalid tax year and invalid Nino " in new Test() {

        val expectedDesResponse = DesResponse("X-123", MultipleErrors(Seq(TaxYearFormatError, NinoFormatError)))
        val nino = "AA123456A"
        val desTaxYear = DesTaxYear("1234")

        MockedHttpClient.get[RetrieveCharitableGivingConnectorOutcome](
          s"$url/income-tax/nino/$nino/income-source/charity/annual/$desTaxYear", dummyDesHeaderCarrierConfig)
          .returns(Future.successful(Left(expectedDesResponse)))

        val result = await(connector.retrieve(RetrieveCharitableGivingRequest(Nino(nino), desTaxYear)))

        result shouldBe Left(expectedDesResponse)
      }
    }
  }
}