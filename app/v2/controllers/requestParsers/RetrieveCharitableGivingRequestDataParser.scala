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

package v2.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import v2.controllers.requestParsers.validators.RetrieveCharitableGivingValidator
import v2.models.errors.{BadRequestError, ErrorWrapper}
import v2.models.requestData.{DesTaxYear, RetrieveCharitableGivingRequest, RetrieveCharitableGivingRawData}

class RetrieveCharitableGivingRequestDataParser @Inject()(validator: RetrieveCharitableGivingValidator) {

  def parseRequest(data: RetrieveCharitableGivingRawData): Either[ErrorWrapper, RetrieveCharitableGivingRequest] = {
    validator.validate(data) match {
      case List() => Right(RetrieveCharitableGivingRequest(Nino(data.nino), DesTaxYear(data.taxYear)))
      case error :: Nil => Left(ErrorWrapper(None, error, None))
      case errors => Left(ErrorWrapper(None, BadRequestError, Some(errors)))
    }
  }

}
