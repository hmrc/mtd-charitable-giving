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
    )
  }

  private def levelTwoValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      JsonFormatValidation.validate[AmendCharitableGiving](data.body),
      MtdTaxYearValidation.validate(data.taxYear, TaxYearNotSpecifiedRuleError)
    )
  }

  private def levelThreeValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      // AmendCharitableGivingEmptyFieldsValidator.validate(data.body)
    )
  }

  private def levelFourValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {

    val amendCharitableGiving = data.body.json.as[AmendCharitableGiving]
    val giftAidPayments = amendCharitableGiving.giftAidPayments
    val gifts = amendCharitableGiving.gifts

    List(
      AmountValidation.validate(giftAidPayments.specifiedYear, GiftAidSpecifiedYearFormatError),
      AmountValidation.validate(giftAidPayments.oneOffSpecifiedYear, GiftAidOneOffSpecifiedYearFormatError),
      AmountValidation.validate(giftAidPayments.specifiedYearTreatedAsPreviousYear, GiftAidSpecifiedYearPreviousFormatError),
      AmountValidation.validate(giftAidPayments.followingYearTreatedAsSpecifiedYear, GiftAidFollowingYearSpecifiedFormatError),
      AmountValidation.validate(giftAidPayments.nonUKCharities, GiftAidNonUKCharityAmountFormatError),
      AmountValidation.validate(gifts.sharesOrSecurities, GiftsSharesSecuritiesFormatError),
      AmountValidation.validate(gifts.landAndBuildings, GiftsLandsBuildingsFormatError),
      AmountValidation.validate(gifts.investmentsNonUKCharities, GiftsInvestmentsAmountFormatError),
      BothFieldsDefinedValidation.validate(giftAidPayments.nonUKCharities, giftAidPayments.nonUKCharityNames, NonUKAmountNotSpecifiedRuleError)
    )

  }

  override def validate(data: AmendCharitableGivingRequestData): List[MtdError] = {
    run(validationSet, data)
  }

}