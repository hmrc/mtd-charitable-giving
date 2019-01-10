/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v2.models.requestData.DesTaxYear
import v2.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CharitableGivingISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String
    val taxYear: String

    def setupStubs(): StubMapping

    def request(): WSRequest = {  // This should be DES tests, as charitable giving will integrate with DES
      setupStubs()
      buildRequest(s"/2.0/$nino/charitable-giving/$taxYear")
    }
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

  "Calling the amend charitable giving endpoint" should {

    "return a 204 status code" when {

      "any valid request is made" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.amendSuccess(nino, DesTaxYear(taxYear).toDesTaxYear)
        }

        val response: WSResponse = await(request().put(Json.parse(charitableGivingWithNonUKCharityDonations)))
        response.status shouldBe Status.NO_CONTENT
      }
    }
  }
}
