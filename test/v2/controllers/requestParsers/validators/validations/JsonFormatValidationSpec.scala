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

import play.api.libs.json.{Json, Reads}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v2.models.errors.Error
import v2.models.utils.JsonErrorValidators

class JsonFormatValidationSpec extends UnitSpec with JsonErrorValidators {

  case class Person(
                     fullName: String,
                     totalWorth: BigDecimal,
                     namesOfChildren: List[String],
                     employed: Boolean,
                     favouriteBook: Book,
                     topSecretPassword: Option[String]
                   )

  case class Book(title: String, author: String)

  implicit val bookReads: Reads[Book] = Json.reads[Book]
  implicit val personReads: Reads[Person] = Json.reads[Person]

  "validate" should {
    "return no errors" when {
      "a valid JSON object with all the necessary fields is supplied" in {

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.isEmpty shouldBe true

      }

      "a valid JSON object with optional fields missing is supplied" in {

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    }
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.isEmpty shouldBe true

      }
    }

    "return an error" when {

      "a required field is missing" in {

        val expectedError = Error("JSON_FIELD_MISSING", "'totalWorth' is missing")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "a string type is required but another type is provided" in {

        val expectedError = Error("JSON_STRING_EXPECTED", "'fullName' should be a valid JSON string")

        val json =
          """
            |{
            |    "fullName": 101010101110,
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "a number type is required but another type is provided" in {

        val expectedError = Error("JSON_NUMBER_EXPECTED", "'totalWorth' should be a valid JSON number")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": "Timothy James Barnes",
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "a boolean type is required but another type is provided" in {

        val expectedError = Error("JSON_BOOLEAN_EXPECTED", "'employed' should be a valid JSON boolean")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": "Yes Sir",
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "an object is required but another type is provided" in {

        val expectedError = Error("JSON_OBJECT_EXPECTED", "'favouriteBook' should be a valid JSON object")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": "Kite Runner",
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "an array is required but another type is provided" in {
        val expectedError = Error("JSON_ARRAY_EXPECTED", "'namesOfChildren' should be a valid JSON array")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": "Timmy and Tommy",
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError

      }

      "an error occurs below the first level of the json data" in {
        val expectedError = Error("JSON_FIELD_MISSING", "'favouriteBook/title' is missing")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 1
        validationResult.head shouldBe expectedError
      }

    }

    "return multiple errors" when {

      "invalid types are provided in an array" in {

        val expectedErrorOne = Error("JSON_STRING_EXPECTED", "'namesOfChildren(0)' should be a valid JSON string")
        val expectedErrorTwo = Error("JSON_STRING_EXPECTED", "'namesOfChildren(1)' should be a valid JSON string")
        val expectedErrorThree = Error("JSON_STRING_EXPECTED", "'namesOfChildren(2)' should be a valid JSON string")

        val json =
          """
            |{
            |    "fullName": "Timothy James Barnes",
            |    "totalWorth": 1234567.88,
            |    "namesOfChildren": [ 1, 2, 3 ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)
        validationResult.size shouldBe 3
        validationResult.contains(expectedErrorOne) shouldBe true
        validationResult.contains(expectedErrorTwo) shouldBe true
        validationResult.contains(expectedErrorThree) shouldBe true

      }

      "multiple incorrect types are provided" in {

        val expectedErrorOne = Error("JSON_STRING_EXPECTED", "'fullName' should be a valid JSON string")
        val expectedErrorTwo = Error("JSON_NUMBER_EXPECTED", "'totalWorth' should be a valid JSON number")

        val json =
          """
            |{
            |    "fullName": 1234567.88,
            |    "totalWorth": "Timothy James Barnes",
            |    "namesOfChildren": [
            |        "Arthur",
            |        "Jarthur",
            |        "Barthur",
            |        "Narthur"
            |    ],
            |    "employed": true,
            |    "favouriteBook": {
            |        "title": "A Thousand Splendid Suns",
            |        "author": "Khaled Hosseini"
            |    },
            |    "topSecretPassword": "foobarfoobar123"
            |}
          """.stripMargin
        val jsonInput = AnyContentAsJson(Json.parse(json))

        val validationResult = JsonFormatValidation.validate[Person](jsonInput)

        validationResult.size shouldBe 2
        validationResult.contains(expectedErrorOne) shouldBe true
        validationResult.contains(expectedErrorTwo) shouldBe true

      }

    }

  }

}
