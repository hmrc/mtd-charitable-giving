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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, AnyContentAsJson}
import v2.controllers.requestParsers.AmendCharitableGivingRequestDataParser
import v2.models.requestData.AmendCharitableGivingRequestData
import v2.services.{CharitableGivingService, EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CharitableGivingController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           charitableGivingService: CharitableGivingService,
                                           amendCharitableGivingRequestDataParser: AmendCharitableGivingRequestDataParser
                                          ) extends AuthorisedController {

  def retrieve(nino: String, taxYear: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>
    Future.successful(Ok(request.userDetails.mtdId))
  }

  def amend(nino: String, taxYear: String): Action[JsValue] = authorisedAction(nino).async(parse.json) { implicit request =>

    amendCharitableGivingRequestDataParser.parseRequest(
      AmendCharitableGivingRequestData(nino, taxYear, AnyContentAsJson(request.body))) match {

      case Right(amendCharitableGivingRequest) => charitableGivingService.amend(amendCharitableGivingRequest).map {
        case Right(correlationId) => NoContent.withHeaders("X-CorrelationId" -> correlationId)
      }
    }
  }
}
