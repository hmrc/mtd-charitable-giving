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
import play.api.mvc.{Action, AnyContent, AnyContentAsJson}
import v2.controllers.requestParsers.{AmendCharitableGivingRequestDataParser, RetrieveCharitableGivingRequestDataParser}
import v2.models.errors._
import v2.models.requestData.{AmendCharitableGivingRequestData, RetrieveCharitableGivingRequestData}
import v2.services.{CharitableGivingService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CharitableGivingController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           charitableGivingService: CharitableGivingService,
                                           amendCharitableGivingRequestDataParser: AmendCharitableGivingRequestDataParser,
                                           retrieveCharitableGivingRequestDataParser: RetrieveCharitableGivingRequestDataParser
                                          ) extends AuthorisedController {

  val logger: Logger = Logger(this.getClass)

  def amend(nino: String, taxYear: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>

    amendCharitableGivingRequestDataParser.parseRequest(AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(request.body))) match {

      case Right(amendCharitableGivingRequest) => charitableGivingService.amend(amendCharitableGivingRequest).map {
        case Right(correlationId) =>
          logger.info(s"[CharitableGivingController][amend] - Success response received with correlationId: $correlationId")
          NoContent.withHeaders("X-CorrelationId" -> correlationId)
        case Left(errorWrapper) => processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
      case Left(errorWrapper) => Future.successful {
        processError(errorWrapper).withHeaders("X-CorrelationId" -> getCorrelationId(errorWrapper))
      }
    }
  }

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>

    retrieveCharitableGivingRequestDataParser.parseRequest(RetrieveCharitableGivingRequestData(nino, taxYear)) match {
      case Right(retrieveCharitableGivingRequest) =>
        charitableGivingService.retrieve(retrieveCharitableGivingRequest).map {
          case Right(desResponse) =>
            logger.info(s"[CharitableGivingController][retrieve] - Success response received with correlationId: ${desResponse.correlationId}")
            Ok(Json.toJson(desResponse.responseData)).withHeaders("X-CorrelationId" -> desResponse.correlationId)
        }
    }
  }

  private def processError(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError
           | NinoFormatError
           | TaxYearFormatError
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
           | TaxYearNotSpecifiedRuleError => BadRequest(Json.toJson(errorWrapper))
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
}
