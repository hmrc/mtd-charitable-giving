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

import v2.controllers.requestParsers.validators.validations._
import v2.models.AmendCharitableGiving
import v2.models.errors._
import v2.models.requestData.AmendCharitableGivingRequestData

class AmendCharitableGivingValidator extends Validator[AmendCharitableGivingRequestData] {

  private val validationSet = List(levelOneValidations, levelTwoValidations, levelThreeValidations, levelFourValidations)

  private def levelOneValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
        //Converted input data validation
        JsonFormatValidation.validate[AmendCharitableGiving](data.body)
    )
  }

  private def levelTwoValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(

    )
  }

  private def levelThreeValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      AmendCharitableGivingEmptyFieldsValidator.validate(data.body)
        val myparsed = json.pars
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
      AmountValidator(giftAidPayments.specifiedYear, SPECIFIED_YEAR_ERROR)
    )
  }

  private def levelFourValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      AmendCharitableGivingDataValidator.validate(data.body)
    )
  }

  override def validate(data: AmendCharitableGivingRequestData): List[MtdError] = {
    run(validationSet, data) match {
      case Nil => List()
        //TODO
      /**
        * Add back in during unhappy path
        *
      case err :: Nil => Left(List(err))
      case errs => Left(errs)
        **/
    }
  }
}