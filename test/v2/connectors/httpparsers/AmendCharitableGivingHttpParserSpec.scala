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

package v2.connectors.httpparsers

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.models.errors._
import v2.models.outcomes
import v2.models.outcomes.DesResponse


class AmendCharitableGivingHttpParserSpec extends UnitSpec {

  val method = "PUT"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = Json.obj("transactionReference" -> transactionReference)
  val desResponse = DesResponse("X-123", transactionReference)

  val correlationId = "X-123"

  "read" should {
    "return a DesResponse" when {
      "the http response contains a 200" in {

        val httpResponse = HttpResponse(OK, Some(desExpectedJson), Map("CorrelationId" -> Seq(correlationId)))

        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Right(desResponse)
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
        val expected = DesResponse(correlationId, SingleError(MtdError("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the http response contains a 403 with an error response body" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected =  DesResponse(correlationId, SingleError(MtdError("TEST_CODE", "some reason")))

        val httpResponse = HttpResponse(FORBIDDEN, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return a generic error" when {

      "the successful json response from DES can't be read" in {

        val httpResponse = HttpResponse(OK, Some(Json.obj("foo" -> "bar")), Map("CorrelationId" -> Seq(correlationId)))
        val expected =  DesResponse(correlationId, GenericError(DownstreamError))


        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the error response from DES can't be read" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "foo": "TEST_CODE",
            |  "bar": "some reason"
            |}
          """.
            stripMargin)
        val expected =  DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }

      "the error response status code is not one that is handle" in {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "foo": "TEST_CODE",
            |  "bar": "some reason"
            |}
          """.
            stripMargin)
        val expected =  DesResponse(correlationId, GenericError(DownstreamError))
        val unHandledStatusCode = SEE_OTHER

        val httpResponse = HttpResponse(unHandledStatusCode, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
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
        val expected =  DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
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
        val expected =  DesResponse(correlationId, GenericError(DownstreamError))

        val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(PUT, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }

    "return multiple errors" when {
      "the http response contains a 400 with an error response body with multiple errors" in {
        val errorResponseJson = Json.parse(
          """
            | [
            |    {
            |      "code": "TEST_CODE_1",
            |      "reason": "some reason"
            |    },
            |    {
            |      "code": "TEST_CODE_2",
            |      "reason": "some reason"
            |    }
            |  ]
          """.stripMargin)
        val expected =  DesResponse(correlationId, MultipleErrors(Seq(MtdError("TEST_CODE_1", "some reason"), MtdError("TEST_CODE_2", "some reason"))))

        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(POST, "/test", httpResponse)
        result shouldBe Left(expected)
      }
      "the http response contains a 403 with an error response body with multiple errors" in {
        val errorResponseJson = Json.parse(
          """
            | [
            |    {
            |      "code": "TEST_CODE_1",
            |      "reason": "some reason"
            |    },
            |    {
            |      "code": "TEST_CODE_2",
            |      "reason": "some reason"
            |    }
            |  ]
          """.stripMargin)
        val expected =  DesResponse(correlationId, MultipleErrors(Seq(MtdError("TEST_CODE_1", "some reason"), MtdError("TEST_CODE_2", "some reason"))))

        val httpResponse = HttpResponse(FORBIDDEN, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(POST, "/test", httpResponse)
        result shouldBe Left(expected)
      }
    }
  }
}
