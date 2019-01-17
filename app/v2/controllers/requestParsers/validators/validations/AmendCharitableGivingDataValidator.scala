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

package v2.controllers.requestParsers.validators.validations

import play.api.mvc.AnyContentAsJson
import v2.models.{AmendCharitableGiving, GiftAidPayments, Gifts, errors}
import v2.models.errors._

object AmendCharitableGivingDataValidator {
  val nameFormat = "^[^|]{1,75}$".r


  private def validateGiftAidNonUKCharityNames(nonUKCharities: Option[BigDecimal], nonUKCharityNames: Option[Seq[String]],
                                        resultList: List[MtdError]): List[MtdError] = {
    resultList.::((nonUKCharities.exists(x => x > 0), nonUKCharityNames.isEmpty) match {
      case (true, true) => NonUKNamesNotSpecifiedRuleError
      case (false, false) => NonUKAmountNotSpecifiedRuleError
      case (true, false) if !nonUKCharityNames.exists(list => list.forall(x => nameFormat.pattern.matcher(x).matches)) => GiftAidNonUKNamesFormatError
    })

    resultList
  }

  private def validateGiftsNonUKCharityNames(investmentsNonUKCharities: Option[BigDecimal], investmentsNonUKCharityNames: Option[Seq[String]],
                                        resultList: List[MtdError]): List[MtdError] = {
    resultList.::((investmentsNonUKCharities.exists(x => x > 0), investmentsNonUKCharityNames.isEmpty) match {
      case (true, true) => NonUKInvestmentsNamesNotSpecifiedRuleError
      case (false, false) => NonUKInvestmentAmountNotSpecifiedRuleError
      case (true, false) if !investmentsNonUKCharityNames.exists(list => list.forall(x => nameFormat.pattern.matcher(x).matches)) =>
        GiftsNonUKInvestmentsNamesFormatError
    })

    resultList
  }
}
