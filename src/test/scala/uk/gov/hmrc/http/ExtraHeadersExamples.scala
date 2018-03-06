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
import org.apache.commons.codec.binary.Base64
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.utils._
import uk.gov.hmrc.play.test.UnitSpec


class ExtraHeadersExamples extends UnitSpec with ScalaFutures with IntegrationPatience with WiremockTestServer {
  implicit val uw = User.writes
  implicit val uir = UserIdentifier.reads

  val myHttpClient = new MyHttpClient(None, StandaloneWSClient.client) {

    // The default implementation doesn't allow setting headers on calls to hosts external to mdtp. 
    override def applicableHeaders(url: String)(implicit hc: HeaderCarrier): Seq[(String, String)] = hc.headers
  }

  "A verb" should {

    import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
    implicit val reads = BankHolidays.reads

    "allow the user to set additional headers using the header carrier" in {

      implicit val hc = HeaderCarrier(otherHeaders = Seq("some-header" -> "header value"))

      stubFor(get("/bank-holidays.json")
        .willReturn(ok(JsonPayloads.bankHolidays)))

      myHttpClient.GET[BankHolidays]("http://localhost:20001/bank-holidays.json").futureValue

      verify(getRequestedFor(urlEqualTo("/bank-holidays.json"))
        .withHeader("some-header", equalTo("header value")))

    }

    "allow the use to set an authorization header using the header carrier" in {

      val username = "user"
      val password = "123"
      val encodedAuthHeader = Base64.encodeBase64String(s"$username:$password".getBytes("UTF-8"))
      implicit val hc = HeaderCarrier(authorization = Some(Authorization(s"Basic $encodedAuthHeader")))

      stubFor(get("/bank-holidays.json")
        .willReturn(ok(JsonPayloads.bankHolidays)))

      myHttpClient.GET[BankHolidays]("http://localhost:20001/bank-holidays.json").futureValue

      verify(getRequestedFor(urlEqualTo("/bank-holidays.json"))
        .withHeader("Authorization", equalTo("Basic dXNlcjoxMjM=")))

    }

    "allow the user to set additional headers as part of the POST" in {

      implicit val hc = HeaderCarrier()

      stubFor(post("/create-user")
        .willReturn(ok(JsonPayloads.bankHolidays)))

      val user = User("me@mail.com", "John Smith")

      myHttpClient.POST[User, HttpResponse]("http://localhost:20001/create-user", user,
        headers = Seq("some-header" -> "header value")).futureValue

      verify(postRequestedFor(urlEqualTo("/create-user"))
        .withHeader("some-header", equalTo("header value")))

    }

    "allow the use to set an authorization header as part of a header in the POST and override the Authorization header in the headerCarrier" in {

      implicit val hc = HeaderCarrier(authorization = None)

      stubFor(post("/create-user")
        .willReturn(ok(JsonPayloads.bankHolidays)))

      val user = User("me@mail.com", "John Smith")

      myHttpClient.POST[User, HttpResponse]("http://localhost:20001/create-user", user,
        headers = Seq("Authorization" -> "Basic dXNlcjoxMjM=")).futureValue

      verify(postRequestedFor(urlEqualTo("/create-user"))
        .withHeader("Authorization", equalTo("Basic dXNlcjoxMjM=")))

    }

  }
}
