package uk.gov.hmrc.play

import play.api.libs.json.Writes
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks

import scala.concurrent.Future

// N.B
// the following import adds the previous GET, POST, etc functions to CoreXXX traits that perform
// reads to convert the HttpResponse to the desired type (backward compatibility with previous http-verbs library)
import uk.gov.hmrc.play.http.ws.WSExtensions._


object Examples {

  import uk.gov.hmrc.play.http.ws._

  trait ConnectorWithHttpValues {
    val http: HttpGet with HttpPost
  }

  object ConnectorWithHttpValues extends ConnectorWithHttpValues {
    val http = new WSGet with HttpGet with WSPut with HttpPut with WSPost with HttpPost with WSDelete with HttpDelete with WSPatch with HttpPatch with HttpHooks {
      val hooks = NoneRequired
    }
  }

  trait ConnectorWithMixins extends HttpGet with HttpPost {
  }

  object ConnectorWithMixins extends ConnectorWithMixins with WSGet with WSPost {
    val hooks = NoneRequired

    override def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

    override def doPatch[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???

    override def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???
  }

  trait VerbExamples {
    val http: HttpGet with HttpPost with HttpPut with HttpDelete with HttpPatch

    implicit val hc = HeaderCarrier()

    http.GET("http://gov.uk/hmrc")
    http.DELETE("http://gov.uk/hmrc")
    http.POST("http://gov.uk/hmrc", body = "hi there")
    http.PUT("http://gov.uk/hmrc", body = "hi there")
    http.PATCH("http://gov.uk/hmrc", body = "hi there")

    val r1 = http.GET("http://gov.uk/hmrc") // Returns an HttpResponse
    val r2 = http.GET[HttpResponse]("http://gov.uk/hmrc") // Can specify this explicitly
    r1.map { r =>
      r.status
      r.body
      r.allHeaders
    }

    import play.api.libs.json._
    case class MyCaseClass(a: String, b: Int)
    implicit val f = Json.reads[MyCaseClass]
    http.GET[MyCaseClass]("http://gov.uk/hmrc") // Returns an MyCaseClass de-serialised from JSON

    import play.twirl.api.Html
    http.GET[Html]("http://gov.uk/hmrc") // Returns a Play Html type

    http.GET[Option[MyCaseClass]]("http://gov.uk/hmrc") // Returns None, or Some[MyCaseClass] de-serialised from JSON
    http.GET[Option[Html]]("http://gov.uk/hmrc") // Returns a None, or a Play Html type
  }
}
