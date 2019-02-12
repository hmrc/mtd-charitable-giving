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
import v2.fixtures.Fixtures.CharitableGivingFixture.charitableGivingModel
import v2.fixtures.Fixtures._
import v2.models.domain.{CharitableGiving, GiftAidPayments, Gifts}
import v2.models.errors._
import v2.models.requestData.AmendCharitableGivingRawData

class AmendCharitableGivingValidatorSpec extends UnitSpec {

  val validNino = "AA123456A"
  val validTaxYear = "2017-18"
  val validJsonBody = AnyContentAsJson(CharitableGivingFixture.mtdFormatJson)


  private trait Test {
    val validator = new AmendCharitableGivingValidator()
  }

  def createJson(amendCharitableGiving: CharitableGiving): JsValue = {

    val giftAidPayments = amendCharitableGiving.giftAidPayments.map(data => Json.obj("giftAidPayments" -> Json.writes[GiftAidPayments].writes(data)))
    val gifts = amendCharitableGiving.gifts.map(data => Json.obj("gifts" -> Json.writes[Gifts].writes(data)))

    List(giftAidPayments, gifts).foldLeft(Json.obj())((a, b) => if (b.isDefined) a ++ b.get else a)

  }

  "running a validation" should {

    "return no errors" when {
      "when the uri is valid and the JSON payload is Valid with all fields" in new Test {
        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, validJsonBody)

        val result: Seq[Error] = validator.validate(inputData)

        result shouldBe List()

      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is greater than zero and " +
        "giftAidPayments.nonUKCharityNames is provided " in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(11.00)),
              nonUKCharityNames = Some(List("A", "B"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result shouldBe List()

      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is equal to zero and " +
        "giftAidPayments.nonUKCharityNames is not provided " in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(0.00)),
              nonUKCharityNames = None
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result shouldBe List()

      }

      "the gifts field is missing and the giftAidPayments field is provided" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = None
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)
        result shouldBe List()

      }

      "the giftAidPayments field is missing and the gifts field is provided" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = None
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)
        result shouldBe List()

      }
    }

    "return a single error" when {

      "specifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              specifiedYear = Some(BigDecimal(-11))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidSpecifiedYearFormatError

      }

      "oneOffSpecifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              oneOffSpecifiedYear = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidOneOffSpecifiedYearFormatError

      }

      "specifiedYearTreatedAsPreviousYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              specifiedYearTreatedAsPreviousYear = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidSpecifiedYearPreviousFormatError

      }

      "followingYearTreatedAsSpecifiedYear of giftAidPayments is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              followingYearTreatedAsSpecifiedYear = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidFollowingYearSpecifiedFormatError

      }

      "nonUKCharities of giftAidPayments is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidNonUKCharityAmountFormatError

      }

      "sharesOrSecurities of gifts is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              sharesOrSecurities = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsSharesSecuritiesFormatError

      }

      "landAndBuildings of gifts is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              landAndBuildings = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsLandsBuildingsFormatError

      }

      "investmentsNonUKCharities of gifts is not a valid value" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsInvestmentsAmountFormatError

      }

      "the supplied tax year is before 2017" in new Test {

        val invalidTaxYear = "2015-16"
        val inputData = AmendCharitableGivingRawData(validNino, invalidTaxYear, AnyContentAsJson(createJson(charitableGivingModel)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe TaxYearNotSpecifiedRuleError
      }

      "giftAidPayments.nonUKCharities is supplied with greater than 0 amount but giftAidPayments.nonUKCharityNames is not supplied " in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(charitableGivingModel.giftAidPayments.get.copy(
            nonUKCharities = Some(BigDecimal(12.34)),
            nonUKCharityNames = None
          )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKNamesNotSpecifiedRuleError

      }

      "giftAidPayments.nonUKCharities is supplied with greater than 0 amount but giftAidPayments.nonUKCharityNames is supplied but is empty" in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(12.34)),
              nonUKCharityNames = Some(List())
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKNamesNotSpecifiedRuleError

      }

      "giftAidPayments.nonUKCharities is supplied with amount equal to 0 " +
        "and giftAidPayments.nonUKCharityNames is supplied " in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(0)),
              nonUKCharityNames = Some(List("A", "B"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKAmountNotSpecifiedRuleError

      }

      "giftAidPayments.nonUKCharities is not supplied and giftAidPayments.nonUKCharityNames is supplied" in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = None,
              nonUKCharityNames = Some(List("A", "B"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKAmountNotSpecifiedRuleError

      }

      "giftAidPayments.nonUKCharities is supplied with value(s) not meeting the regex" in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(12.34)),
              nonUKCharityNames = Some(Seq("|||INVALID||||", "VALID", "VALID"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidNonUKNamesFormatError

      }

      "giftAidPayments.nonUKCharities is provided and giftAidPayments.nonUKCharities is greater than zero and " +
        "giftAidPayments.nonUKCharityNames is not provided " in new Test {
        val mutatedData = charitableGivingModel.copy(
          giftAidPayments = Some(
            charitableGivingModel.giftAidPayments.get.copy(
              nonUKCharities = Some(BigDecimal(11.00)),
              nonUKCharityNames = None
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKNamesNotSpecifiedRuleError
      }

      "gifts.investmentsNonUKCharities is supplied with greater than 0 amount but gifts.investmentsNonUKCharityNames is not supplied " in new Test {
        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = Some(BigDecimal(12.34)),
              investmentsNonUKCharityNames = None
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKInvestmentsNamesNotSpecifiedRuleError

      }

      "gifts.investmentsNonUKCharities is supplied with amount equal to 0 " +
        "and gifts.investmentsNonUKCharityNames is supplied " in new Test {
        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = Some(BigDecimal(0)),
              investmentsNonUKCharityNames = Some(List("A", "B"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKInvestmentAmountNotSpecifiedRuleError

      }

      "gifts.investmentsNonUKCharities is not supplied and gifts.investmentsNonUKCharityNames is supplied" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = None,
              investmentsNonUKCharityNames = Some(List("A", "B"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKInvestmentAmountNotSpecifiedRuleError

      }

      "gifts.investmentsNonUKCharities is supplied with value(s) not meeting the regex" in new Test {

        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = Some(BigDecimal(12.34)),
              investmentsNonUKCharityNames = Some(Seq("|||INVALID||||", "VALID", "VALID"))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftsNonUKInvestmentsNamesFormatError

      }

      "gifts.investmentsNonUKCharities is provided and giftAidPayments.investmentsNonUKCharities is greater than zero and " +
        "gifts.investmentsNonUKCharityNames is not provided " in new Test {
        val mutatedData = charitableGivingModel.copy(
          gifts = Some(charitableGivingModel.gifts.get.copy(
            investmentsNonUKCharities = Some(BigDecimal(11.00)),
            investmentsNonUKCharityNames = None
          )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKInvestmentsNamesNotSpecifiedRuleError
      }

      "gifts.investmentsNonUKCharities is provided and giftAidPayments.investmentsNonUKCharities is greater than zero and " +
        "gifts.investmentsNonUKCharityNames is supplied but is empty " in new Test {
        val mutatedData = charitableGivingModel.copy(
          gifts = Some(charitableGivingModel.gifts.get.copy(
            investmentsNonUKCharities = Some(BigDecimal(11.00)),
            investmentsNonUKCharityNames = Some(List())
          ))
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe NonUKInvestmentsNamesNotSpecifiedRuleError
      }

      "gifts or gift aid are not provided" in new Test {
        val mutatedData = charitableGivingModel.copy(gifts = None, giftAidPayments = None)

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe EmptyOrNonMatchingBodyRuleError
      }

      "gifts has been provided without values" in new Test {
        val mutatedData = charitableGivingModel.copy(gifts = Some(Gifts(None,None,None,None)), giftAidPayments = None)

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result = validator.validate(inputData)

        println(s"\n$result\n")

        result.size shouldBe 1
        result.head shouldBe GiftAidAndGiftsEmptyRuleError
      }

      "giftAidPayments has been provided without values" in new Test {
        val mutatedData = charitableGivingModel.copy(gifts = Some(Gifts(None,None,None,None)), giftAidPayments = None)

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))
        val result = validator.validate(inputData)

        result.size shouldBe 1
        result.head shouldBe GiftAidAndGiftsEmptyRuleError
      }
    }

    "return multiple errors" when {
      "multiple validations fail" in new Test {
        val mutatedData = charitableGivingModel.copy(
          gifts = Some(
            charitableGivingModel.gifts.get.copy(
              investmentsNonUKCharities = Some(BigDecimal(-1)),
              sharesOrSecurities = Some(BigDecimal(-1))
            )
          )
        )

        val inputData = AmendCharitableGivingRawData(validNino, validTaxYear, AnyContentAsJson(createJson(mutatedData)))

        val result: Seq[Error] = validator.validate(inputData)

        result.size shouldBe 2
        result.contains(GiftsInvestmentsAmountFormatError) shouldBe true
        result.contains(GiftsSharesSecuritiesFormatError) shouldBe true

      }
    }

  }

}
