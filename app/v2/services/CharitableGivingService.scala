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

package v2.services

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.DesConnector
import v2.models.errors._
import v2.models.outcomes.DesResponse
import v2.models.requestData.{AmendCharitableGivingRequest, RetrieveCharitableGivingRequest}

import scala.concurrent.{ExecutionContext, Future}

class CharitableGivingService @Inject()(connector: DesConnector) {

  val logger: Logger = Logger(this.getClass)

  def amend(amendCharitableGivingRequest: AmendCharitableGivingRequest)
            (implicit hc: HeaderCarrier,
             ec: ExecutionContext): Future[AmendCharitableGivingOutcome] = {

    connector.amend(amendCharitableGivingRequest).map {
      case Left(DesResponse(correlationId, MultipleErrors(errors))) =>
        val mtdErrors = errors.map(error => desErrorToMtdErrorAmend(error.code))
        if (mtdErrors.contains(DownstreamError)) {
          logger.info(s"[CharitableGivingService] [amend] [CorrelationId - $correlationId]" +
            s" - downstream returned INVALID_IDTYPE or NOT_FOUND_PERIOD. Revert to ISE")
          Left(ErrorWrapper(Some(correlationId), DownstreamError, None))
        } else {
          Left(ErrorWrapper(Some(correlationId), BadRequestError, Some(mtdErrors)))
        }
      case Left(DesResponse(correlationId, SingleError(error))) => Left(ErrorWrapper(Some(correlationId), desErrorToMtdErrorAmend(error.code), None))
      case Left(DesResponse(correlationId, OutboundError(error))) => Left(ErrorWrapper(Some(correlationId), error, None))
      case Right(desResponse) => Right(desResponse)
    }
  }

  def retrieve(retrieveCharitableGivingRequest: RetrieveCharitableGivingRequest)
              (implicit hc: HeaderCarrier,
               ec: ExecutionContext): Future[RetrieveCharitableGivingOutcome] = {

    connector.retrieve(retrieveCharitableGivingRequest).map {
      case Left(DesResponse(correlationId, MultipleErrors(errors))) =>
        val mtdErrors = errors.map(error => desErrorToMtdErrorRetrieve(error.code))
        if (mtdErrors.contains(DownstreamError)) {
          logger.info(s"[CharitableGivingService] [retrieve] [CorrelationId - $correlationId]" +
            s" - downstream returned INVALID_IDTYPE, NOT_FOUND_PERIOD or INVALID_INCOME_SOURCE. Revert to ISE")
          Left(ErrorWrapper(Some(correlationId), DownstreamError, None))
        } else {
          Left(ErrorWrapper(Some(correlationId), BadRequestError, Some(mtdErrors)))
        }
      case Left(DesResponse(correlationId, OutboundError(error))) => Left(ErrorWrapper(Some(correlationId), error, None))
      case Left(DesResponse(correlationId, SingleError(error))) => Left(ErrorWrapper(Some(correlationId), desErrorToMtdErrorRetrieve(error.code), None))
      case Right(desResponse) => Right(DesResponse(desResponse.correlationId, desResponse.responseData))
    }
  }

  private def desErrorToMtdErrorRetrieve: Map[String, Error] = Map(
    "INVALID_NINO" -> NinoFormatError,
    "INVALID_TYPE" -> DownstreamError,
    "INVALID_TAXYEAR" -> TaxYearFormatError,
    "INVALID_INCOME_SOURCE" -> DownstreamError,
    "NOT_FOUND_PERIOD" -> NotFoundError,
    "NOT_FOUND_INCOME_SOURCE" -> NotFoundError,
    "SERVER_ERROR" -> DownstreamError,
    "SERVICE_UNAVAILABLE" -> DownstreamError
  )

  private def desErrorToMtdErrorAmend: Map[String, Error] = Map(
    "INVALID_NINO" -> NinoFormatError,
    "INVALID_TYPE" -> DownstreamError,
    "INVALID_TAXYEAR" -> TaxYearFormatError,
    "INVALID_PAYLOAD" -> BadRequestError,
    "INVALID_ACCOUNTING_PERIOD" -> TaxYearNotSupportedRuleError,
    "NOT_FOUND_INCOME_SOURCE" -> DownstreamError,
    "MISSING_CHARITIES_NAME_GIFT_AID" -> NonUKNamesNotSpecifiedRuleError,
    "MISSING_GIFT_AID_AMOUNT" -> NonUKAmountNotSpecifiedRuleError,
    "MISSING_CHARITIES_NAME_INVESTMENT" -> NonUKInvestmentsNamesNotSpecifiedRuleError,
    "MISSING_INVESTMENT_AMOUNT" -> NonUKInvestmentAmountNotSpecifiedRuleError,
    "SERVER_ERROR" -> DownstreamError,
    "SERVICE_UNAVAILABLE" -> DownstreamError
  )
}
