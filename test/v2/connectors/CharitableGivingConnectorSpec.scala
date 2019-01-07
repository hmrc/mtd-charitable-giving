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
import v2.models.AmendCharitableGivingRequest
import v2.models.inbound.{AmendCharitableGiving, DesTaxYear, GiftAidPayments, Gifts}

class CharitableGivingConnectorSpec extends ConnectorSpec{

  class Test extends MockHttpClient with MockAppConfig {
    val connector = new CharitableGivingConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
    MockedAppConfig.mtdIdBaseUrl returns baseUrl
  }

  lazy val baseUrl = "test-BaseUrl"

"Amend charitable giving tax relief" should {
  "return a successful 204 response" when {
    "a valid request is supplied" in new Test() {
      val result = {
        await(connector.amend(AmendCharitableGivingRequest(Nino("AA123456A"), DesTaxYear(""),
          AmendCharitableGiving(GiftAidPayments(None, None, None, None, None, None), Gifts(None, None, None, None)))))
      }
      result shouldBe Right("success")
    }
  }
}
}
