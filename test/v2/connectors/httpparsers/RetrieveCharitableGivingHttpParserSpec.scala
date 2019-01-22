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

import play.api.libs.json.JsValue
import play.api.test.Helpers.{GET, OK}
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse
import v2.fixtures.Fixtures.CharitableGivingFixture
import v2.models.outcomes.DesResponse

class RetrieveCharitableGivingHttpParserSpec extends UnitSpec {

  val method = "PUT"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson: JsValue = CharitableGivingFixture.desFormatJson
  val httpParsedDesResponse = DesResponse("X-123", CharitableGivingFixture.charitableGivingModel)

  "retrieveHttpReads" should {
    "return a DesResponse with valid mtd charitable giving json" when {
      "the http response has status 200 and des charitable giving json" in {
        val correlationId = "X-123"
        val httpResponse = HttpResponse(OK, Some(desExpectedJson), Map("CorrelationId" -> Seq(correlationId)))

        val result = RetrieveCharitableGivingHttpParser.retrieveHttpReads.read(GET, "/test", httpResponse)
        result shouldBe Right(httpParsedDesResponse)
      }
    }
  }

}
