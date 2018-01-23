/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.http

import com.github.tomakehurst.wiremock.client.WireMock._
import org.joda.time.LocalDate
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import uk.gov.hmrc.http.utils._
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class HttpReadsExamples extends UnitSpec with ScalaFutures with IntegrationPatience with WiremockTestServer {

  class CustomException(message: String) extends Exception(message) {}

  val myHttpClient = new MyHttpClient(None, StandaloneWSClient.client)

  implicit val hc = HeaderCarrier()
  implicit val reads = BankHolidays.reads

  implicit val responseHandler = new HttpReads[Option[BankHolidays]] {
    override def read(method: String, url: String, response: HttpResponse): Option[BankHolidays] = {
      response.status match {
        case 200 => Try(response.json.as[BankHolidays]) match {
          case Success(data) => Some(data)
          case Failure(e) => throw new CustomException("Unable to parse response")
        }
        case 404 => None
        case unexpectedStatus => throw new CustomException(s"Unexpected response code '$unexpectedStatus'")
      }
    }
  }

  "HttpReads" should {

    "Return some data when getting a 200 back" in {
      stubFor(get("/bank-holidays.json")
        .willReturn(ok(JsonPayloads.bankHolidays)))

      val bankHolidays = myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/bank-holidays.json").futureValue
      bankHolidays.get.events.head shouldBe BankHoliday("New Year’s Day", new LocalDate(2017, 1, 2))
    }

    "Fail when the response payload cannot be deserialised" in {
      stubFor(get("/bank-holidays.json")
        .willReturn(ok("Not json")))

      a[CustomException] shouldBe thrownBy {
        await(myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/bank-holidays.json"))
      }
    }

    "Return None when getting a 404 back" in {
      stubFor(get("/bank-holidays.json")
        .willReturn(aResponse().withStatus(404)))

      val bankHolidays = myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/bank-holidays.json").futureValue
      bankHolidays shouldBe None
    }

    "Fail if we get back any other status code" in {
      stubFor(get("/bank-holidays.json")
        .willReturn(aResponse().withStatus(400)))

      a[CustomException] shouldBe thrownBy {
        await(myHttpClient.GET[Option[BankHolidays]]("http://localhost:20001/bank-holidays.json"))
      }
    }
  }
}
