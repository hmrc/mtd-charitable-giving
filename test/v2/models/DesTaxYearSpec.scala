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

package v2.models

import play.api.libs.json.JsString
import support.UnitSpec
import v2.models.inbound.DesTaxYear
import v2.models.utils.JsonErrorValidators

class DesTaxYearSpec extends UnitSpec with JsonErrorValidators {

  "reads" should {
    "create the correct model" in {
      DesTaxYear.taxYearRead.reads(JsString.apply("2017-18")).get shouldBe DesTaxYear("2017-18")
    }
  }

  "writes" should {
    "write the tax year in the correct format" in {
      DesTaxYear.taxYearWrite.writes(DesTaxYear("2017-18")) shouldBe JsString.apply("2018")
    }
  }

}
