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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v2.fixtures.Fixtures.AmendCharitableGivingFixture.amendCharitableGivingModel
import v2.fixtures.Fixtures._
import v2.models.errors._
import v2.models.requestData.AmendCharitableGivingRequestData
import v2.models.{AmendCharitableGiving, GiftAidPayments, Gifts}

class AmendCharitableGivingValidatorSpec extends UnitSpec {

  val validNino = "AA123456A"
  val validTaxYear = "2017-18"
  val validJsonBody = AnyContentAsJson(AmendCharitableGivingFixture.inputJson)


  private trait Test {
    val validator = new AmendCharitableGivingValidator()
  }

  def createJson(amendCharitableGiving: AmendCharitableGiving): JsValue = {
    Json.obj(
      "giftAidPayments" -> Json.writes[GiftAidPayments].writes(amendCharitableGiving.giftAidPayments),
      "gifts" -> Json.writes[Gifts].writes(amendCharitableGiving.gifts)
    )
  }

  "running a validation" should {

    "return no errors" when {
      "when the uri is valid and the JSON payload is Valid" in new Test {
        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, validJsonBody)

        val result: Seq[MtdError] = validator.validate(inputData)

        result shouldBe List()

      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is greater than zero and " +
        "giftAidPayments.nonUKCharityNames is provided " in new Test {
        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(11.00)),
            nonUKCharityNames = Some(List("A", "B"))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[MtdError] = validator.validate(inputData)

        result shouldBe List()

      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is equal to zero and " +
        "giftAidPayments.nonUKCharityNames is not provided " in new Test {
        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(0.00)),
            nonUKCharityNames = None
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[MtdError] = validator.validate(inputData)

        result shouldBe List()

      }


    }

    "return a single error" when {

      "specifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            specifiedYear = Some(BigDecimal(-11))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidSpecifiedYearFormatError

      }

      "oneOffSpecifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            oneOffSpecifiedYear = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidOneOffSpecifiedYearFormatError

      }

      "specifiedYearTreatedAsPreviousYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            specifiedYearTreatedAsPreviousYear = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidSpecifiedYearPreviousFormatError

      }

      "followingYearTreatedAsSpecifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            followingYearTreatedAsSpecifiedYear = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidFollowingYearSpecifiedFormatError

      }

      "nonUKCharities of giftAidPayments is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidNonUKCharityAmountFormatError

      }

      "sharesOrSecurities of gifts is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          gifts = amendCharitableGivingModel.gifts.copy(
            sharesOrSecurities = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsSharesSecuritiesFormatError

      }

      "landAndBuildings of gifts is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          gifts = amendCharitableGivingModel.gifts.copy(
            landAndBuildings = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsLandsBuildingsFormatError

      }

      "investmentsNonUKCharities of gifts is not a valid value" in new Test {

        val mutatedData = amendCharitableGivingModel.copy(
          gifts = amendCharitableGivingModel.gifts.copy(
            investmentsNonUKCharities = Some(BigDecimal(-1))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsInvestmentsAmountFormatError

      }

      "giftAidPayments.nonUKCharities is supplied with greater than 0 amount but giftAidPayments.nonUKCharityNames is not supplied " in new Test {
        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(12.34)),
            nonUKCharityNames = None
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKNamesNotSpecifiedRuleError

      }

      "giftAidPayments.nonUKCharities is supplied with value(s) not meeting the regex" in new Test {
        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(12.34)),
            nonUKCharityNames = Some(Seq("|||INVALID||||", "VALID", "VALID"))
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidNonUKNamesFormatError

      }



      // TODO REVIEW FOR opposite way field
//      "giftAidPayments.nonUKCharityNames is supplied but giftAidPayments.nonUKCharities is not supplied " in new Test {
//        val mutatedData = amendCharitableGivingModel.copy(
//          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
//            nonUKCharities = None,
//            nonUKCharityNames = Some(List("CHARITY A", "CHARITY B"))
//          )
//        )
//
//        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
//
//        val result: Seq[MtdError] = validator.validate(inputData)
//
//        result.size shouldBe 1
//        result.head shouldBe NonUKAmountNotSpecifiedRuleError
//      }

      "the supplied tax year is before 2017" in new Test {
        val invalidTaxYear = "2015-16"
        val inputData = AmendCharitableGivingRequestData(validNino, invalidTaxYear, AnyContentAsJson(createJson(amendCharitableGivingModel)))

        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe TaxYearNotSpecifiedRuleError
      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is greater than zero and " +
        "giftAidPayments.nonUKCharityNames is not provided " in new Test {
        val mutatedData = amendCharitableGivingModel.copy(
          giftAidPayments = amendCharitableGivingModel.giftAidPayments.copy(
            nonUKCharities = Some(BigDecimal(11.00)),
            nonUKCharityNames = None
          )
        )

        val inputData = AmendCharitableGivingRequestData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[MtdError] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKNamesNotSpecifiedRuleError
      }

    }

    "return multiple errors" when {
      // TODO
    }

  }

}
