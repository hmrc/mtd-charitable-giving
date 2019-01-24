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

package v2.services

import uk.gov.hmrc.domain.Nino
import v2.fixtures.Fixtures.CharitableGivingFixture._
import v2.mocks.connectors.MockDesConnector
import v2.models.outcomes.DesResponse
import v2.models.requestData.{DesTaxYear, RetrieveCharitableGivingRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCharitableGivingServiceSpec extends ServiceSpec {

  trait Test extends MockDesConnector {
    lazy val target = new CharitableGivingService(connector)
  }


  val correlationId = "X-123"
  val nino = "AA123456A"
  val taxYear = "2017-18"
  val expectedDesResponse = DesResponse(correlationId, charitableGivingModel)
  val input = RetrieveCharitableGivingRequest(Nino(nino), DesTaxYear(taxYear))
  val output = DesResponse(correlationId, charitableGivingModel)

  "calling retrieve" should {
    "return a valid correlationId" when {
      "a valid data is passed" in new Test {

        MockedDesConnector.retrieve(input).returns(Future.successful(Right(expectedDesResponse)))

        private val result = await(target.retrieve(input))

        result shouldBe Right(output)
      }
    }
  }
}
