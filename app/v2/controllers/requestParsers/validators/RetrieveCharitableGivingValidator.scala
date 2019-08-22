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

package v2.controllers.requestParsers.validators

import v2.controllers.requestParsers.validators.validations.{MtdTaxYearValidation, NinoValidation, TaxYearValidation}
import v2.models.errors.{Error, TaxYearNotSupportedRuleError}
import v2.models.requestData.RetrieveCharitableGivingRawData

class RetrieveCharitableGivingValidator extends Validator[RetrieveCharitableGivingRawData] {

  private val validationSet = List(levelOneValidations, levelTwoValidations)

  private def levelOneValidations: RetrieveCharitableGivingRawData => List[List[Error]] =
    (data: RetrieveCharitableGivingRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TaxYearValidation.validate(data.taxYear)
      )
    }

  private def levelTwoValidations: RetrieveCharitableGivingRawData => List[List[Error]] =
    (data: RetrieveCharitableGivingRawData) => {
      List(
        MtdTaxYearValidation.validate(data.taxYear, TaxYearNotSupportedRuleError)
      )
    }

  override def validate(data: RetrieveCharitableGivingRawData): List[Error] = {
    run(validationSet, data).distinct
  }

}
