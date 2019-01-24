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
import v2.models.CharitableGiving
import v2.models.errors._
import v2.models.requestData.AmendCharitableGivingRequestData

class AmendCharitableGivingValidator extends Validator[AmendCharitableGivingRequestData] {

  private val validationSet = List(levelOneValidations, levelTwoValidations, levelThreeValidations)

  private def levelOneValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def levelTwoValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    List(
      JsonFormatValidation.validate[CharitableGiving](data.body),
      MtdTaxYearValidation.validate(data.taxYear, TaxYearNotSpecifiedRuleError)
    )
  }

  private def levelThreeValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {

    val amendCharitableGiving = data.body.json.as[CharitableGiving]

    val giftAidPaymentsValidations = if (amendCharitableGiving.giftAidPayments.nonEmpty) {
      val giftAidPayments = amendCharitableGiving.giftAidPayments.get

      lazy val nonUKNamesNotSpecifiedRuleErrorCheck =
        giftAidPayments.nonUKCharities.exists(_ > 0 && (giftAidPayments.nonUKCharityNames.isEmpty || giftAidPayments.nonUKCharityNames.get.isEmpty))
      lazy val namesSuppliedButIncorrectAmountCheck =
        giftAidPayments.nonUKCharityNames.exists(_.nonEmpty && giftAidPayments.nonUKCharities.forall(_ == 0))

      List(
        AmountValidation.validate(giftAidPayments.specifiedYear, GiftAidSpecifiedYearFormatError),
        AmountValidation.validate(giftAidPayments.oneOffSpecifiedYear, GiftAidOneOffSpecifiedYearFormatError),
        AmountValidation.validate(giftAidPayments.specifiedYearTreatedAsPreviousYear, GiftAidSpecifiedYearPreviousFormatError),
        AmountValidation.validate(giftAidPayments.followingYearTreatedAsSpecifiedYear, GiftAidFollowingYearSpecifiedFormatError),
        AmountValidation.validate(giftAidPayments.nonUKCharities, GiftAidNonUKCharityAmountFormatError),
        PredicateValidation.validate(nonUKNamesNotSpecifiedRuleErrorCheck, NonUKNamesNotSpecifiedRuleError),
        PredicateValidation.validate(namesSuppliedButIncorrectAmountCheck, NonUKAmountNotSpecifiedRuleError),
        ArrayElementsRegexValidation.validate(giftAidPayments.nonUKCharityNames, "^[^|]{1,75}$", GiftAidNonUKNamesFormatError)
      )
    } else {
      List()
    }

    val giftsValidations = if (amendCharitableGiving.gifts.isDefined) {

      val gifts = amendCharitableGiving.gifts.get

      lazy val nonUKInvestmentsNamesNotSpecifiedRuleErrorCheck =
        gifts.investmentsNonUKCharities.exists(_ > 0 && (gifts.investmentsNonUKCharityNames.isEmpty || gifts.investmentsNonUKCharityNames.get.isEmpty))
      lazy val investmentsNamesSuppliedButIncorrectAmountCheck =
        gifts.investmentsNonUKCharityNames.exists(_.nonEmpty && gifts.investmentsNonUKCharities.forall(_ == 0))

      List(
        AmountValidation.validate(gifts.sharesOrSecurities, GiftsSharesSecuritiesFormatError),
        AmountValidation.validate(gifts.landAndBuildings, GiftsLandsBuildingsFormatError),
        AmountValidation.validate(gifts.investmentsNonUKCharities, GiftsInvestmentsAmountFormatError),
        PredicateValidation.validate(nonUKInvestmentsNamesNotSpecifiedRuleErrorCheck, NonUKInvestmentsNamesNotSpecifiedRuleError),
        PredicateValidation.validate(investmentsNamesSuppliedButIncorrectAmountCheck, NonUKInvestmentAmountNotSpecifiedRuleError),
        ArrayElementsRegexValidation.validate(gifts.investmentsNonUKCharityNames, "^[^|]{1,75}$", GiftsNonUKInvestmentsNamesFormatError)
      )
    } else {
      List()
    }

    giftAidPaymentsValidations ++ giftsValidations
  }


  override def validate(data: AmendCharitableGivingRequestData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}