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

package v2.connectors

import uk.gov.hmrc.domain.Nino
import v2.mocks.{MockAppConfig, MockHttpClient}
import v2.models.outcomes.{AmendCharitableGivingConnectorOutcome, DesResponse}
import v2.models.requestData.{AmendCharitableGivingRequest, DesTaxYear}
import v2.models.{AmendCharitableGiving, GiftAidPayments, Gifts}

import scala.concurrent.Future

class DesConnectorSpec extends ConnectorSpec{

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new DesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  lazy val baseUrl = "test-BaseUrl"

  "Amend charitable giving tax relief" should {
    "return a successful 204 response" when {
      "a valid request is supplied" in new Test() {

        val expectedRef = "000000000001013"
        val nino = "AA123456A"
        val taxYear = "2017-18"

        val expectedDesResponse = DesResponse("X-123", expectedRef)

        MockedHttpClient.post[AmendCharitableGiving, AmendCharitableGivingConnectorOutcome](
          s"$baseUrl" + s"/income-tax/nino/$nino/income-source/charity/annual/${DesTaxYear(taxYear).toDesTaxYear}",
          AmendCharitableGiving(GiftAidPayments(None, None, None, None, None, None), Gifts(None, None, None, None)))
          .returns(Future.successful(Right(expectedDesResponse)))

        val result = await(connector.amend(AmendCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear),
            AmendCharitableGiving(GiftAidPayments(None, None, None, None, None, None), Gifts(None, None, None, None)))))

        result shouldBe Right(expectedDesResponse)
      }
    }
  }
}
