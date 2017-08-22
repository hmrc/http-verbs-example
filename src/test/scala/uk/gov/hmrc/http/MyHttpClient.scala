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

import com.typesafe.config.Config
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.utils.StandaloneWSClient
import uk.gov.hmrc.play.http.ws._

class MyHttpClient extends HttpGet with WSGet
    with HttpPut with WSPut
    with HttpDelete with WSDelete
    with HttpPost with WSPost
    with HttpPatch with WSPatch {

  // Add your hooks here. For example an auditing hook
  override val hooks: Seq[HttpHook] = Seq.empty

  // If you are using Dependency Injection, you can provide your application configuration here.
  // I'm providing no additional configuration as it is not relevant for this test, do not copy this line
  override lazy val configuration: Option[Config] = None

  // If you are using Dependency Injection, you can provide the WS client here.
  // I'm using StandaloneWSClient.client for testing purposes, do not copy this line
  override lazy val wsClient: WSClient = StandaloneWSClient.client

}
