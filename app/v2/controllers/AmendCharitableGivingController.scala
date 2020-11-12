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

package v2.controllers

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v2.controllers.requestParsers.AmendCharitableGivingRequestDataParser
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, CharitableGivingAuditDetail}
import v2.models.auth.UserDetails
import v2.models.errors._
import v2.models.requestData.AmendCharitableGivingRawData
import v2.services.{AuditService, CharitableGivingService, EnrolmentsAuthService, MtdIdLookupService}
import v2.utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AmendCharitableGivingController @Inject()(val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                requestDataParser: AmendCharitableGivingRequestDataParser,
                                                charitableGivingService: CharitableGivingService,
                                                auditService: AuditService,
                                                cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "AmendCharitableGivingController", endpointName = "amend")

  def amend(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      val rawData = AmendCharitableGivingRawData(nino, taxYear, AnyContentAsJson(request.body))
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestDataParser.parseRequest(rawData))
          desResponse <- EitherT(charitableGivingService.amend(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${desResponse}")
          auditSubmission(createAuditDetails(nino, taxYear, NO_CONTENT, request.request.body,
            desResponse.correlationId, request.userDetails))

          NoContent
            .withApiHeaders(desResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result        = errorResult(errorWrapper).withApiHeaders(correlationId)
        auditSubmission(createAuditDetails(nino, taxYear, result.header.status, request.request.body, correlationId, request.userDetails, Some(errorWrapper)))
        errorResult(errorWrapper).withApiHeaders(correlationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
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
           | TaxYearNotSupportedRuleError
           | RuleTaxYearRangeExceededError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
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
