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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.xml.XML

class HttpResponseExamples extends UnitSpec with ScalaFutures with IntegrationPatience with WiremockTestServer {

  class CustomException(message: String) extends Exception(message) {}

  val myHttpClient = new MyHttpClient(None, StandaloneWSClient.client)

  implicit val hc = HeaderCarrier()

  "Get using HttpResponse" should {

    def fromXml(xml: String): BankHolidays =
      BankHolidays((XML.loadString(xml) \ "event") map { event => {
          BankHoliday((event \ "title").text, LocalDate.parse((event \ "date").text)) }})

    def responseHandler(response: HttpResponse) : Option[BankHolidays] = {
      response.status match {
        case 200 => Try(fromXml(response.body)) match {
          case Success(data) => Some(data)
          case Failure(e) =>
            throw new CustomException("Unable to parse response")
        }
      }
    }

    "Return some data when getting a 200 back" in {
      stubFor(get("/bank-holidays.xml")
        .willReturn(ok(XmlPayloads.bankHolidays)))

      val bankHolidays = myHttpClient.GET[HttpResponse]("http://localhost:20001/bank-holidays.xml")
          .map(responseHandler).futureValue

      bankHolidays.get.events.head shouldBe BankHoliday("New Year’s Day", new LocalDate(2017, 1, 2))
    }

    "Fail when the response payload cannot be deserialised" in {
      stubFor(get("/bank-holidays.xml")
        .willReturn(ok("Not xml")))

      a[CustomException] shouldBe thrownBy {
        await(myHttpClient.GET[HttpResponse]("http://localhost:20001/bank-holidays.xml")
          .map(responseHandler))
      }
    }
  }
}
