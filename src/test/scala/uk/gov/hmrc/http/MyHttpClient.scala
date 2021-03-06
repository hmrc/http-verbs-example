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

import com.typesafe.config.Config
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._

class MyHttpClient(config: Option[Config], client: WSClient) extends HttpGet with WSGet
    with HttpPut with WSPut
    with HttpDelete with WSDelete
    with HttpPost with WSPost
    with HttpPatch with WSPatch {

  // Extend this class to add your hooks. For example an auditing hook
  override val hooks: Seq[HttpHook] = Seq.empty

  override lazy val configuration: Option[Config] = config

  override lazy val wsClient: WSClient = client

}
