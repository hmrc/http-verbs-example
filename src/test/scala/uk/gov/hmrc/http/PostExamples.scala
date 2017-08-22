/*
 * Copyright 2017 HM Revenue & Customs
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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import uk.gov.hmrc.http.utils._
import uk.gov.hmrc.play.test.UnitSpec


class PostExamples extends UnitSpec with ScalaFutures with IntegrationPatience with WiremockTestServer {

  val myHttpClient = new MyHttpClient

  "A POST" should {

    import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
    implicit val hc = HeaderCarrier()
    implicit val uw = User.writes
    implicit val uir = UserIdentifier.reads

    "write a case class to json body and return a response" in {

      stubFor(post("/create-user")
        .willReturn(noContent))
      val user = User("me@mail.com", "John Smith")

      // Use HttpResponse when the API always returns an empty body
      val response: HttpResponse = myHttpClient.POST[User, HttpResponse]("http://localhost:20001/create-user", user).futureValue
      response.status shouldBe 204
    }

    "read the response body of the POST into a case class" in {

      stubFor(post("/create-user")
        .willReturn(ok(JsonPayloads.userId)))
      val user = User("me@mail.com", "John Smith")

      // Use a case class when the API returns a json body
      val userId: UserIdentifier = myHttpClient.POST[User, UserIdentifier]("http://localhost:20001/create-user", user).futureValue
      userId.id shouldBe "123"
    }

    "be able to handle both 204 and 200 in the same configuration" in {

      stubFor(post("/create-user")
        .willReturn(noContent))
      val user = User("me@mail.com", "John Smith")

      // Use Option[T], where T is your case class, if the API might return both 200 and 204
      val userId: Option[UserIdentifier] = myHttpClient.POST[User, Option[UserIdentifier]]("http://localhost:20001/create-user", user).futureValue
      userId shouldBe None
    }

  }
}
