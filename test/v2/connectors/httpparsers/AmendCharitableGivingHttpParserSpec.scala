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
import play.api.test.Helpers.OK
import support.UnitSpec
import uk.gov.hmrc.http.HttpResponse


class AmendCharitableGivingHttpParserSpec extends UnitSpec{

  val method = "GET"
  val url = "test-url"

  val transactionReference = "000000000001"
  val desExpectedJson = Json.obj("transactionReference" -> transactionReference)

  "read" should {
    "return the transactionReference" when {
      "des returns a valid success response" in {
        val desResponse = HttpResponse(OK, Some(desExpectedJson))
        val result = AmendCharitableGivingHttpParser.amendHttpReads.read(method, url, desResponse)
        result shouldBe Right(transactionReference)
      }
    }
  }

}
