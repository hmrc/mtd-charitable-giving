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

package v2.connectors.httpparsers

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.models.errors.{DownstreamError, OutboundError, Error, SingleError}
import v2.models.outcomes.DesResponse

class RetrieveCharitableGivingHttpParserSpec extends UnitSpec {

  val method = "PUT"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = CharitableGivingFixture.desFormatJson
  val httpParsedDesResponse = DesResponse("X-123", CharitableGivingFixture.charitableGivingModel)
  val correlationId = "X-123"

  "retrieveHttpReads" should {

    "return a DesResponse with valid mtd charitable giving json" when {
      "the http response has status 200 and des charitable giving json" in {
        val correlationId = "X-123"
        val httpResponse = HttpResponse(OK, desExpectedJson, Map("CorrelationId" -> Seq(correlationId)))

        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(GET, "/test", httpResponse)
        result shouldBe Right(httpParsedDesResponse)
      }
    }

    "return a single error" when {
      "the http response contains a 400 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(Error("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson, Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(GET, "/", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 404 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = DesResponse(correlationId, SingleError(Error("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(NOT_FOUND, errorResponseJson, Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(GET, "/", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return a generic error" when {

      "the error response status code is not one that is handled" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "foo": "TEST_CODE",
            |  "bar": "some reason"
            |}
          """.
            stripMargin)
        val expected =  DesResponse(correlationId, OutboundError(DownstreamError))
        val unHandledStatusCode = SEE_OTHER

        val httpResponse = HttpResponse(unHandledStatusCode, errorResponseJson, Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }


      "the error response from DES can't be read" in {
        val expected =  DesResponse(correlationId, OutboundError(DownstreamError))

        val httpResponse = HttpResponse(OK, "", Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 500 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected =  DesResponse(correlationId, OutboundError(DownstreamError))

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, errorResponseJson, Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 503 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.
            stripMargin)
        val expected =  DesResponse(correlationId, OutboundError(DownstreamError))

        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, errorResponseJson, Map("CorrelationId" -> Seq(correlationId)))
        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

    }

  }

}
