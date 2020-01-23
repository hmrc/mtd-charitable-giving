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
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import v2.controllers.requestParsers.RetrieveCharitableGivingRequestDataParser
import v2.models.domain.CharitableGiving
import v2.models.errors._
import v2.models.requestData.RetrieveCharitableGivingRawData
import v2.services.{CharitableGivingService, EnrolmentsAuthService, MtdIdLookupService}
import v2.utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCharitableGivingController @Inject()(val authService: EnrolmentsAuthService,
                                                   val lookupService: MtdIdLookupService,
                                                   requestDataParser: RetrieveCharitableGivingRequestDataParser,
                                                   service: CharitableGivingService,
                                                   cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveCharitableGivingController", endpointName = "retrieve")

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async {
    implicit request =>
      val rawData = RetrieveCharitableGivingRawData(nino, taxYear)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestDataParser.parseRequest(rawData))
          vendorResponse <- EitherT(service.retrieve(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${vendorResponse.correlationId}")

          Ok(Json.toJson(vendorResponse.responseData)(CharitableGiving.desToMtdWrites))
            .withApiHeaders(vendorResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        errorResult(errorWrapper).withApiHeaders(correlationId)
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case  BadRequestError
            | NinoFormatError
            | TaxYearFormatError
            | TaxYearNotSupportedRuleError
            | RuleTaxYearRangeExceededError =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
