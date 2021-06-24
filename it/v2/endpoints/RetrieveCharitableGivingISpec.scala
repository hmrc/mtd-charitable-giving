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

package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.IntegrationBaseSpec
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.models.errors._
import v2.models.requestData.DesTaxYear
import v2.stubs.{ AuditStub, AuthStub, DesStub, MtdIdLookupStub }

class RetrieveCharitableGivingISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String
    val taxYear: String
    val correlationId = "X-123"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/2.0/ni/$nino/charitable-giving/$taxYear")
    }
  }

  "Calling the retrieve charitable giving endpoint" should {

    "return status 200 with valid body" when {

      "any valid request is made" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.retrieveSuccess(nino, DesTaxYear.fromMtd(taxYear))
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe CharitableGivingFixture.mtdFormatJson
      }
    }

    "return 500 (Internal Server Error)" when {

      retrieveErrorTest(Status.BAD_REQUEST, "INVALID_TYPE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      retrieveErrorTest(Status.BAD_REQUEST, "INVALID_INCOME_SOURCE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      retrieveErrorTest(Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError)
      retrieveErrorTest(Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError)
    }

    "return 400 (Bad Request)" when {
      retrieveErrorTest(Status.BAD_REQUEST, "INVALID_NINO", Status.BAD_REQUEST, NinoFormatError)
      retrieveErrorTest(Status.BAD_REQUEST, "INVALID_TAXYEAR", Status.BAD_REQUEST, TaxYearFormatError)
    }

    "return 404 (Not Found)" when {
      retrieveErrorTest(Status.NOT_FOUND, "NOT_FOUND_INCOME_SOURCE", Status.NOT_FOUND, NotFoundError)
      retrieveErrorTest(Status.NOT_FOUND, "NOT_FOUND_PERIOD", Status.NOT_FOUND, NotFoundError)
    }

    "return a 400 (Bad Request) with multiple errors" when {

      val multipleErrors: String =
        s"""
           |{
           |	"failures" : [
           |      {
           |        "code": "INVALID_NINO",
           |        "reason": "Does not matter."
           |      },
           |      {
           |        "code": "INVALID_TAXYEAR",
           |        "reason": "Does not matter."
           |      }
           |  ]
           |}
      """.stripMargin

      s"des returns multiple errors" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.retrieveError(nino, DesTaxYear.fromMtd(taxYear), BAD_REQUEST, multipleErrors)
        }

        val response: WSResponse = await(request().get)
        response.status shouldBe Status.BAD_REQUEST
        response.json shouldBe Json.toJson(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }

    def retrieveErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: Error): Unit = {
      s"des returns an $desCode error" in new Test {
        override val nino: String = "AA123456A"
        override val taxYear: String = "2018-19"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.retrieveError(nino, DesTaxYear.fromMtd(taxYear), desStatus, errorBody(desCode))
        }

        val response: WSResponse = await(request().get)
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
      }
    }
  }

  def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "Does not matter."
       |      }
      """.stripMargin
}