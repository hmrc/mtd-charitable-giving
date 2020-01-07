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

package v2.mocks.connectors

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.{AmendCharitableGivingConnectorOutcome, DesConnector, RetrieveCharitableGivingConnectorOutcome}
import v2.models.requestData.{AmendCharitableGivingRequest, RetrieveCharitableGivingRequest}

import scala.concurrent.{ExecutionContext, Future}

trait MockDesConnector extends MockFactory {

  val connector: DesConnector = mock[DesConnector]

  object MockedDesConnector {
    def amend(amendCharitableGivingRequest: AmendCharitableGivingRequest): CallHandler[Future[AmendCharitableGivingConnectorOutcome]] = {
      (connector.amend(_: AmendCharitableGivingRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(amendCharitableGivingRequest, *, *)
    }

    def retrieve(retrieveCharitableGivingRequest: RetrieveCharitableGivingRequest): CallHandler[Future[RetrieveCharitableGivingConnectorOutcome]] = {
      (connector.retrieve(_: RetrieveCharitableGivingRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(retrieveCharitableGivingRequest, *, *)
    }
  }

}
