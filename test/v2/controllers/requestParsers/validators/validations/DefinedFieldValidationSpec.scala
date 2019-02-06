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

import support.UnitSpec
import v2.models.errors.GiftAidAndGiftsEmptyRuleError
import v2.models.utils.JsonErrorValidators
import v2.models.{CharitableGiving, GiftAidPayments, Gifts}
import v2.fixtures.Fixtures.CharitableGivingFixture._
class DefinedFieldValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "top level optional fields exist" in {

        val validModel = charitableGivingModel
        val validationResult = DefinedFieldValidation.validate(validModel.gifts, validModel.giftAidPayments)
        validationResult shouldBe List()
      }
    }

    "return an error" in {
      val invalidModel = CharitableGiving(None, None)
      DefinedFieldValidation.validate(invalidModel.gifts, invalidModel.giftAidPayments) shouldBe List(GiftAidAndGiftsEmptyRuleError)
    }
  }
}
