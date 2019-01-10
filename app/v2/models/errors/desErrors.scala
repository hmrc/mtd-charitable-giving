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

import play.api.libs.json._
import v2.models.errors.DesErrorCode.DesErrorCode

sealed trait DesError

case class SingleError(error: MtdError) extends DesError
case class MultipleErrors(errors: Seq[MtdError]) extends DesError
case class BvrErrors(errors: Seq[MtdError]) extends DesError
case class GenericError(error: MtdError) extends DesError

object DesErrorCode extends Enumeration {
  type DesErrorCode = Value

  //400
  val INVALID_NINO, INVALID_TYPE, INVALID_TAXYEAR, INVALID_PAYLOAD = Value

  //403
  val NOT_FOUND_INCOME_SOURCE, MISSING_CHARITIES_NAME_GIFT_AID, MISSING_GIFT_AID_AMOUNT,
  MISSING_CHARITIES_NAME_INVESTMENT, MISSING_INVESTMENT_AMOUNT = Value

  //5xx
  val SERVER_ERROR,
  SERVICE_UNAVAILABLE: DesErrorCode = Value

  implicit val desErrorReads: Reads[DesErrorCode] = Reads.enumNameReads(DesErrorCode)
}

object ErrorCode {
  val reads: Reads[Option[DesErrorCode]] = (__ \ "code").readNullable[DesErrorCode]

  def unapply(arg: Option[JsValue]): Option[DesErrorCode] = {
    arg match {
      case Some(json) => reads.reads(json).fold(_ => None, valid => valid)
      case _ => None
    }
  }
}