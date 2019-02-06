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

package v2.models.errors

// Nino Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The provided NINO is invalid")

//Format Rules
object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")
object GiftAidSpecifiedYearFormatError extends MtdError("FORMAT_GIFT_AID_SPECIFIED_YEAR", "The specified year payment amount is invalid")
object GiftAidOneOffSpecifiedYearFormatError extends MtdError("FORMAT_GIFT_AID_ONE_OFF_SPECIFIED_YEAR", "The one-off specified year amount is invalid")
object GiftAidSpecifiedYearPreviousFormatError extends
  MtdError("FORMAT_GIFT_AID_SPECIFIED_YEAR_PREVIOUS_YEAR", "The specified year treated as previous year amount is invalid")
object GiftAidFollowingYearSpecifiedFormatError extends
  MtdError("FORMAT_GIFT_AID_FOLLOWING_YEAR_SPECIFIED_YEAR", "The following year treated as specified year amount is invalid")
object GiftAidNonUKCharityAmountFormatError extends MtdError("FORMAT_GIFT_AID_NONUK_CHARITY_AMOUNT", "The gift aid non-UK Charities amount is invalid")
object GiftAidNonUKNamesFormatError extends MtdError("FORMAT_GIFT_AID_NONUK_NAMES", "The non-UK charity names are invalid")
object GiftsSharesSecuritiesFormatError extends MtdError("FORMAT_GIFTS_SHARES_SECURITIES", "The shares or securities amount is invalid")
object GiftsLandsBuildingsFormatError extends MtdError("FORMAT_GIFTS_LAND_BUILDINGS", "The land and buildings amount is invalid")
object GiftsInvestmentsAmountFormatError extends MtdError("FORMAT_GIFTS_INVESTMENTS_AMOUNT", "The investments amount is invalid")
object GiftsNonUKInvestmentsNamesFormatError extends MtdError("FORMAT_GIFTS_NONUK_INVESTMENTS_NAMES", "The non-UK investments charity names list is invalid")

//Rule Errors
object GiftAidAndGiftsEmptyRuleError extends
  MtdError("RULE_EMPTY_GIFTS_OR_GIFT_AID", "A non-empty gift aid or gifts object must be supplied")
object NonUKNamesNotSpecifiedRuleError extends
  MtdError("RULE_GIFT_AID_NONUK_AMOUNT_WITHOUT_NAMES", "Non-UK charity gift aid amount supplied without the non-UK gift aid charity names")
object NonUKAmountNotSpecifiedRuleError extends
  MtdError("RULE_GIFT_AID_NONUK_NAMES_WITHOUT_AMOUNT", "Non-UK charity gift aid charity names supplied without an amount or the amount was zero")
object NonUKInvestmentsNamesNotSpecifiedRuleError extends
  MtdError("RULE_GIFTS_NONUK_INVESTMENTS_AMOUNT_WITHOUT_NAMES",
    "Positive non-UK gift of investment amount supplied without non-UK gift of investment charity names")
object NonUKInvestmentAmountNotSpecifiedRuleError extends
  MtdError("RULE_GIFTS_NONUK_INVESTMENTS_NAMES_WITHOUT_AMOUNT", "Non-UK gift of investment charity names supplied without an amount or the amount was zero")
object TaxYearNotSpecifiedRuleError extends
  MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "Tax year not supported, because it precedes the earliest allowable tax year")

//Standard Errors
object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")
object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")
object BvrError extends MtdError("BUSINESS_ERROR", "Business validation error")
object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised.")
