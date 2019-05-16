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

package v2.controllers

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v2.controllers.requestParsers.{AmendCharitableGivingRequestDataParser, RetrieveCharitableGivingRequestDataParser}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, CharitableGivingAuditDetail}
import v2.models.auth.UserDetails
import v2.models.domain.CharitableGiving
import v2.models.errors._
import v2.models.requestData.{AmendCharitableGivingRawData, RetrieveCharitableGivingRawData}
import v2.services.{AuditService, CharitableGivingService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CharitableGivingController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           charitableGivingService: CharitableGivingService,
                                           amendCharitableGivingRequestDataParser: AmendCharitableGivingRequestDataParser,
                                           retrieveCharitableGivingRequestDataParser: RetrieveCharitableGivingRequestDataParser,
                                           auditService: AuditService,
                                           cc: ControllerComponents
                                          )(implicit ec: ExecutionContext) extends AuthorisedController(cc) {

  val logger: Logger = Logger(this.getClass)

  def amend(nino: String, taxYear: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>

    amendCharitableGivingRequestDataParser.parseRequest(AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(request.body))) match {

      case Right(amendCharitableGivingRequest) => charitableGivingService.amend(amendCharitableGivingRequest).map {
        case Right(correlationId) =>
          auditSubmission(createAuditDetails(nino, taxYear, NO_CONTENT, request.request.body,
            correlationId, request.userDetails))
          logger.info(s"[CharitableGivingController][amend] - Success response received with correlationId: $correlationId")
          NoContent.withHeaders("X-CorrelationId" -> correlationId)
        case Left(errorWrapper) =>
          val correlationId = getCorrelationId(errorWrapper)
          val result = processError(errorWrapper).withHeaders("X-CorrelationId" -> correlationId)
          auditSubmission(createAuditDetails(nino, taxYear, result.header.status, request.request.body, correlationId, request.userDetails, Some(errorWrapper)))
          result
      }
      case Left(errorWrapper) =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = processError(errorWrapper).withHeaders("X-CorrelationId" -> correlationId)
        auditSubmission(createAuditDetails(nino, taxYear, result.header.status, request.request.body, correlationId, request.userDetails, Some(errorWrapper)))
        Future.successful(result)
    }
  }

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>

    retrieveCharitableGivingRequestDataParser.parseRequest(RetrieveCharitableGivingRawData(nino, taxYear)) match {
      case Right(retrieveCharitableGivingRequest) =>
        charitableGivingService.retrieve(retrieveCharitableGivingRequest).map {
          case Right(desResponse) =>
            logger.info(s"[CharitableGivingController][retrieve] - Success response received with correlationId: ${desResponse.correlationId}")
            Ok(Json.toJson(desResponse.responseData)(CharitableGiving.desToMtdWrites)).withHeaders("X-CorrelationId" -> desResponse.correlationId)
          case Left(errorWrapper) => processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
        }
      case Left(errorWrapper) => Future.successful {
        processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
    }
  }

  private def processError(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError
           | NinoFormatError
           | TaxYearFormatError
           | EmptyOrNonMatchingBodyRuleError
           | GiftAidAndGiftsEmptyRuleError
           | GiftAidSpecifiedYearFormatError
           | GiftAidOneOffSpecifiedYearFormatError
           | GiftAidSpecifiedYearPreviousFormatError
           | GiftAidFollowingYearSpecifiedFormatError
           | GiftAidNonUKCharityAmountFormatError
           | GiftAidNonUKNamesFormatError
           | GiftsSharesSecuritiesFormatError
           | GiftsLandsBuildingsFormatError
           | GiftsInvestmentsAmountFormatError
           | GiftsNonUKInvestmentsNamesFormatError
           | NonUKNamesNotSpecifiedRuleError
           | NonUKAmountNotSpecifiedRuleError
           | NonUKInvestmentsNamesNotSpecifiedRuleError
           | NonUKInvestmentAmountNotSpecifiedRuleError
           | TaxYearNotSpecifiedRuleError
           | RuleTaxYearRangeExceededError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))

    }
  }

  private def getCorrelationId(errorWrapper: ErrorWrapper): String = {
    errorWrapper.correlationId match {
      case Some(correlationId) => logger.info("[CharitableGivingController][getCorrelationId] - " +
        s"Error received from DES ${Json.toJson(errorWrapper)} with correlationId: $correlationId")
        correlationId
      case None =>
        val correlationId = UUID.randomUUID().toString
        logger.info("[CharitableGivingController][getCorrelationId] - " +
          s"Validation error: ${Json.toJson(errorWrapper)} with correlationId: $correlationId")
        correlationId
    }
  }

  private def createAuditDetails(nino: String,
                                 taxYear: String,
                                 statusCode: Int,
                                 request: JsValue,
                                 correlationId: String,
                                 userDetails: UserDetails,
                                 errorWrapper: Option[ErrorWrapper] = None
                                ): CharitableGivingAuditDetail = {
    val auditResponse = errorWrapper.map {
      wrapper =>
        AuditResponse(statusCode, wrapper.allErrors.map(error => AuditError(error.code)))
    }

    CharitableGivingAuditDetail(userDetails.userType, userDetails.agentReferenceNumber, nino, taxYear, request, correlationId, auditResponse)
  }

  private def auditSubmission(details: CharitableGivingAuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("amendCharitableGivingTaxRelief", "update-charitable-giving-annual-summary", details)
    auditService.auditEvent(event)
  }
}
