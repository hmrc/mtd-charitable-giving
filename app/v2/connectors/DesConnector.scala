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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v2.config.AppConfig
import v2.models.CharitableGiving
import v2.models.outcomes.{AmendCharitableGivingConnectorOutcome, RetrieveCharitableGivingConnectorOutcome}
import v2.models.requestData.{AmendCharitableGivingRequest, RetrieveCharitableGivingRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject()(http: HttpClient,
                             appConfig: AppConfig) {

  val logger = Logger(this.getClass)

  private[connectors] def desHeaderCarrier(implicit hc: HeaderCarrier): HeaderCarrier = hc
    .copy(authorization = Some(Authorization(s"Bearer ${appConfig.desToken}")))
    .withExtraHeaders("Environment" -> appConfig.desEnv)

  def amend(amendCharitableGivingRequest: AmendCharitableGivingRequest)
           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AmendCharitableGivingConnectorOutcome] = {

    import v2.connectors.httpparsers.AmendCharitableGivingHttpParser.amendHttpReads
    import v2.models.CharitableGiving.writes

    val nino = amendCharitableGivingRequest.nino.nino
    val taxYear = amendCharitableGivingRequest.desTaxYear.toDesTaxYear

    val url = s"${appConfig.desBaseUrl}/income-tax/nino/$nino/income-source/charity/annual/$taxYear"

    http.POST[CharitableGiving, AmendCharitableGivingConnectorOutcome](url, amendCharitableGivingRequest.model)(writes, amendHttpReads,
      desHeaderCarrier, implicitly)
  }

  def retrieve(retrieveCharitableGivingRequest: RetrieveCharitableGivingRequest)
              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RetrieveCharitableGivingConnectorOutcome] = {

    val nino = retrieveCharitableGivingRequest.nino.nino
    val taxYear = retrieveCharitableGivingRequest.desTaxYear.toDesTaxYear
    import v2.connectors.httpparsers.RetrieveCharitableGivingHttpParser.retrieveHttpReads

    val url = s"${appConfig.desBaseUrl}/income-tax/nino/$nino/income-source/charity/annual/${taxYear}"

    http.GET[RetrieveCharitableGivingConnectorOutcome](url)(retrieveHttpReads, desHeaderCarrier, implicitly)
  }
}
