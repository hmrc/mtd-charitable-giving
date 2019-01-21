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

  private val validationSet = List(levelOneValidations, levelTwoValidations, levelThreeValidations, levelFourValidations, levelFiveValidations)

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

    lazy val nonUKNamesNotSpecifiedRuleErrorCheck = giftAidPayments.nonUKCharities.exists(_ > 0 && giftAidPayments.nonUKCharityNames.isEmpty)

    List(
      AmountValidation.validate(giftAidPayments.specifiedYear, GiftAidSpecifiedYearFormatError),
      AmountValidation.validate(giftAidPayments.oneOffSpecifiedYear, GiftAidOneOffSpecifiedYearFormatError),
      AmountValidation.validate(giftAidPayments.specifiedYearTreatedAsPreviousYear, GiftAidSpecifiedYearPreviousFormatError),
      AmountValidation.validate(giftAidPayments.followingYearTreatedAsSpecifiedYear, GiftAidFollowingYearSpecifiedFormatError),
      AmountValidation.validate(giftAidPayments.nonUKCharities, GiftAidNonUKCharityAmountFormatError),
      AmountValidation.validate(gifts.sharesOrSecurities, GiftsSharesSecuritiesFormatError),
      AmountValidation.validate(gifts.landAndBuildings, GiftsLandsBuildingsFormatError),
      AmountValidation.validate(gifts.investmentsNonUKCharities, GiftsInvestmentsAmountFormatError),
      PredicateValidation.validate(nonUKNamesNotSpecifiedRuleErrorCheck, NonUKNamesNotSpecifiedRuleError),
      ArrayElementsRegexValidation.validate(giftAidPayments.nonUKCharityNames, "^[^|]{1,75}$", GiftAidNonUKNamesFormatError)
    )

  }

  private def levelFiveValidations: AmendCharitableGivingRequestData => List[List[MtdError]] = (data: AmendCharitableGivingRequestData) => {
    //    lazy val exists = Some(BigDecimal(12.00)).nonEmpty
    //    lazy val positive = Predicates.positiveAmount(Some(BigDecimal(12.00)))
    //    lazy val exists = false
    //
    //
    //
    //    //    lazy val positive = Predicates.positiveAmount(None)
    //
    //    val bd = Some(BigDecimal(12.00))
    //    val other = None
    //
    //    lazy val amountExistsAndGreaterThanZeroAndNoNames = bd.map(_ > 0 && other.isEmpty)

    List(
      //      CompoundValidation.validate(bd.exists(_ > 0 && other.isEmpty))
    )


    // TODO DELETE - TEMP SUDO CODE
    //    val amendCharitableGiving = data.body.json.as[AmendCharitableGiving]
    //    val giftAidPayments = amendCharitableGiving.giftAidPayments
    //    val gifts = amendCharitableGiving.gifts
    //
    //    CompoundValidation.validate(
    //      AndValidation(
    //        Predicates.isDefined(giftAidPayments.nonUKCharities)
    //          Predicates.positiveAmount(giftAidPayments.nonUKCharities),
    //      Predicates.isEmpty(giftAidPayments.nonUKCharityNames)
    //    ),
    //    NonUKAmountNotSpecifiedRuleError
    //    )
    //    List()
  }

  override def validate(data: AmendCharitableGivingRequestData): List[MtdError] = {
    // TODO DISTINCT
    // TODO E7 / E8 / E13 / E20 / E14 / E21 / E11 / E15 / E25 / E12 / E16 / E23
    // TODO Length validation on the charity names array
    run(validationSet, data)
  }

}