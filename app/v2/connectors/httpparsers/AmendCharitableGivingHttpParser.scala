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

import play.api.http.Status.NO_CONTENT
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import v2.models.outcomes.AmendCharitableGivingOutcome

object AmendCharitableGivingHttpParser extends HttpParser {

  private val jsonReads: Reads[String] = (__ \ "id").read[String]

  implicit val amendHttpReads: HttpReads[AmendCharitableGivingOutcome] = new HttpReads[AmendCharitableGivingOutcome] {
    override def read(method: String, url: String, response: HttpResponse): AmendCharitableGivingOutcome = {
      response.status match {
        case NO_CONTENT => response.validateJson[String](jsonReads) match {
          case Some(id) => Right(id)
        }
      }
    }
  }
}
