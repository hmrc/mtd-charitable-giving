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

package v2.connectors.httpparsers

import play.api.Logger
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v2.models.errors._
import v2.models.outcomes.{AmendCharitableGivingConnectorOutcome, DesResponse}

object AmendCharitableGivingHttpParser extends HttpParser {

  val logger = Logger(AmendCharitableGivingHttpParser.getClass)


  private val jsonReads: Reads[String] = (__ \ "transactionReference").read[String]

  implicit val amendHttpReads: HttpReads[AmendCharitableGivingConnectorOutcome] = new HttpReads[AmendCharitableGivingConnectorOutcome] {
    override def read(method: String, url: String, response: HttpResponse): AmendCharitableGivingConnectorOutcome = {

      val correlationId = retrieveCorrelationId(response)
      if(response.status != OK) {
        logger.info("[AmendCharitableGivingHttpParser][read] - " +
          s"Error response received from DES with status: ${response.status} and body\n" +
          s"${response.body} and correlationId: $correlationId when calling $url")
      }

      response.status match {
        case OK => logger.info("[AmendCharitableGivingHttpParser][read] - " +
          s"Success response received from DES with correlationId: $correlationId when calling $url")
          parseResponse(correlationId, response)
        case BAD_REQUEST | FORBIDDEN => Left(DesResponse(correlationId,parseErrors(response)))
        case INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE => Left(DesResponse(correlationId, GenericError(DownstreamError)))
      }
    }
    private def parseResponse(correlationId: String, response: HttpResponse): AmendCharitableGivingConnectorOutcome =
      response.validateJson[String](jsonReads) match {
      case Some(ref) => Right(DesResponse(correlationId, ref))
      case None => Left(DesResponse(correlationId, GenericError(DownstreamError)))
    }
  }
}
