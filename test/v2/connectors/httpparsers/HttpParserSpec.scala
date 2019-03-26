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

import play.api.libs.json.Json
import play.api.test.Helpers.BAD_REQUEST
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.models.errors._

class HttpParserSpec extends UnitSpec {

  val correlationId = "X-123"


  "parseErrors" should {
    "return a single error" when {
      "the http response contains a single error " in new HttpParser() {
        val errorResponseJson = Json.parse(
          """
            |{
            |  "code": "TEST_CODE",
            |  "reason": "some reason"
            |}
          """.stripMargin)
        val expected = SingleError(Error("TEST_CODE", "some reason"))
        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))

        val result = parseErrors(httpResponse)

        result shouldBe expected
      }
    }

    "return multiple errors" when {
      "the http response contains multiple errors " in new HttpParser() {
        val errorResponseJson = Json.parse(
          """
            |{
            |	"failures" : [
            |		{
            |			"code": "TEST_CODE_1",
            |  			"reason": "some reason"
            |		},
            |		{
            |			"code": "TEST_CODE_2",
            |  			"reason": "some reason"
            |		}
            |	]
            |}
          """.stripMargin)
        val expected =  MultipleErrors(Seq(
          Error("TEST_CODE_1", "some reason"),
          Error("TEST_CODE_2", "some reason")
        ))
        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))

        val result = parseErrors(httpResponse)

        result shouldBe expected
      }
    }

    "return a single Downstream error" when {
      "the errors JSON can't be parsed" in new HttpParser() {
        val errorResponseJson = Json.parse(
          """
            |{
            |	"unstructured" : "JSON",
            | "that": "parser",
            | "can't" : "understand"
            |}
          """.stripMargin)
        val expected =  OutboundError(DownstreamError)
        val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson), Map("CorrelationId" -> Seq(correlationId)))

        val result = parseErrors(httpResponse)

        result shouldBe expected
      }
    }

  }

}
