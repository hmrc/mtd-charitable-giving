/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.errors

import play.api.libs.json.{ Json, OWrites }

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
    implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

    implicit def genericWrites[T <: MtdError]: OWrites[T] =
        writes.contramap[T](c => c: MtdError)
}

// Nino Errors
object NinoFormatError extends Error(
    code = "FORMAT_NINO",
    message = "The provided NINO is invalid")

//Format Rules
object TaxYearFormatError extends Error(
    code = "FORMAT_TAX_YEAR",
    message = "The provided tax year is invalid")

object GiftAidSpecifiedYearFormatError extends Error(
    code = "FORMAT_GIFT_AID_SPECIFIED_YEAR",
    message = "The specified year payment amount is invalid")

object GiftAidOneOffSpecifiedYearFormatError extends Error(
    code = "FORMAT_GIFT_AID_ONE_OFF_SPECIFIED_YEAR",
    message = "The one-off specified year amount is invalid")

object GiftAidSpecifiedYearPreviousFormatError extends Error(
    code = "FORMAT_GIFT_AID_SPECIFIED_YEAR_PREVIOUS_YEAR",
    message = "The specified year treated as previous year amount is invalid")

object GiftAidFollowingYearSpecifiedFormatError extends Error(
    code = "FORMAT_GIFT_AID_FOLLOWING_YEAR_SPECIFIED_YEAR",
    message = "The following year treated as specified year amount is invalid")

object GiftAidNonUKCharityAmountFormatError extends Error(
    code = "FORMAT_GIFT_AID_NONUK_CHARITY_AMOUNT",
    message = "The gift aid non-UK Charities amount is invalid")

object GiftAidNonUKNamesFormatError extends Error(
    code = "FORMAT_GIFT_AID_NONUK_NAMES",
    message = "The non-UK charity names are invalid")

object GiftsSharesSecuritiesFormatError extends Error(
    code = "FORMAT_GIFTS_SHARES_SECURITIES",
    message = "The shares or securities amount is invalid")

object GiftsLandsBuildingsFormatError extends Error(
    code = "FORMAT_GIFTS_LAND_BUILDINGS",
    message = "The land and buildings amount is invalid")

object GiftsInvestmentsAmountFormatError extends Error(
    code = "FORMAT_GIFTS_INVESTMENTS_AMOUNT",
    message = "The investments amount is invalid")

object GiftsNonUKInvestmentsNamesFormatError extends Error(
    code = "FORMAT_GIFTS_NONUK_INVESTMENTS_NAMES",
    message = "The non-UK investments charity names list is invalid")

//Rule Errors
object EmptyOrNonMatchingBodyRuleError extends Error(
    code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
    message = "An empty or non-matching body was submitted")

object GiftAidAndGiftsEmptyRuleError extends Error(
    code = "RULE_EMPTY_GIFTS_OR_GIFT_AID",
    message = "A supplied gift aid or gifts object must not be empty")

object NonUKNamesNotSpecifiedRuleError extends Error(
    code = "RULE_GIFT_AID_NONUK_AMOUNT_WITHOUT_NAMES",
    message = "Non-UK charity gift aid amount supplied without the non-UK gift aid charity names")

object NonUKAmountNotSpecifiedRuleError extends Error(
    code = "RULE_GIFT_AID_NONUK_NAMES_WITHOUT_AMOUNT",
    message ="Non-UK charity gift aid charity names supplied without an amount or the amount was zero")

object NonUKInvestmentsNamesNotSpecifiedRuleError extends Error(
    code = "RULE_GIFTS_NONUK_INVESTMENTS_AMOUNT_WITHOUT_NAMES",
    message ="Positive non-UK gift of investment amount supplied without non-UK gift of investment charity names")

object NonUKInvestmentAmountNotSpecifiedRuleError extends Error(
    code = "RULE_GIFTS_NONUK_INVESTMENTS_NAMES_WITHOUT_AMOUNT",
    message = "Non-UK gift of investment charity names supplied without an amount or the amount was zero")

object TaxYearNotSupportedRuleError extends Error(
    code = "RULE_TAX_YEAR_NOT_SUPPORTED",
    message = "Tax year not supported, because it precedes the earliest allowable tax year")

object RuleTaxYearRangeExceededError extends Error(
    code = "RULE_TAX_YEAR_RANGE_EXCEEDED",
    message = "Tax year range exceeded. A tax year range of one year is required.")

//Standard Errors
object NotFoundError extends Error(
    code = "MATCHING_RESOURCE_NOT_FOUND",
    message = "Matching resource not found"
)

object DownstreamError extends Error(
    code = "INTERNAL_SERVER_ERROR",
    message = "An internal server error occurred"
)

object BadRequestError extends Error(
    code = "INVALID_REQUEST",
    message = "Invalid request"
)

object BVRError extends Error(
    code = "BUSINESS_ERROR",
    message = "Business validation error"
)

object ServiceUnavailableError extends Error(
    code = "SERVICE_UNAVAILABLE",
    message = "Internal server error"
)

//Authorisation Errors
object UnauthorisedError extends Error(
    code = "CLIENT_OR_AGENT_NOT_AUTHORISED",
    message = "The client and/or agent is not authorised"
)