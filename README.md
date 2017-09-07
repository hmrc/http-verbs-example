
# http-verbs-example

Since http-verbs 7.0.0, the verbs business logic has been refactored to remove dependencies to Play.  
This will allow easier maintenance, upgrades and improvements.  
Play is now just used as an implementation of the transport layer [defined here](https://github.com/hmrc/http-core/blob/master/src/main/scala/uk/gov/hmrc/http/HttpTransport.scala)

This project provides examples of how http-verbs can be used in your service.
There is some basic examples below, but most use cases are explained in the tests [here](https://github.com/hmrc/http-verbs-example/blob/master/src/test/scala/uk/gov/hmrc/http)

## Adding to your build

In your SBT build add:

```scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "http-verbs" % "x.x.x"
libraryDependencies += "uk.gov.hmrc" %% "http-verbs-play-25" % "x.x.x"
```

If you are using http-verbs in one of your services, you are most likely using one of the bootstrap libraries.  
In this case there is no need to add the verbs as they should already be available to you as transitive dependencies

## Create the client

You client class must inherit from both the business logic and the transport layer.

For example, if you are interested in doing a GET with your client, you can do like this:
```scala
class MyHttpClient extends HttpGet with WSGet
```

A more comprehensive example [can be found here](https://github.com/hmrc/http-verbs-example/blob/master/src/test/scala/uk/gov/hmrc/http/MyHttpClient.scala) 

## Make a call

The most common way to use the verbs is when the called endpoint returns JSON.  
The client can deserialise the response into a case class. For example:
```scala
val response: Future[BankHolidays] = client.GET[BankHolidays]("http://localhost/bank-holidays.json")
```

Sometimes an API can return 404, as a valid return status.  
In this case, to avoid a NotFoundException, wrap your case class in an Option. If 404 is returned, the response will be None.
```scala
val response: Future[Option[Something]] = client.GET[Option[Something]]("http://localhost/404.json")
```

If you need more control over how your body is deserialsed, you can ask for an HttpResponse in return. This special case class allows access to the raw details of the request, including content and status code. Please note, this will still be subject to the default error handling provided by HttpVerbs.
```scala
def fromXml(xml: String): BankHolidays =
  BankHolidays((XML.loadString(xml) \ "event") map { event => {
    BankHoliday((event \ "title").text, LocalDate.parse((event \ "date").text)) }})

val response: Future[BankHolidays] = client.GET[HttpResponse]("http://localhost/bank-holidays.xml").map { response =>
  response.status match {
    case 200 => Try(fromXml(response.body)) match {
      case Success(data) => Some(data)
      case Failure(e) =>
        throw new CustomException("Unable to parse response")
    }
  }
}
```

If you want full control over your response, including how status codes are mapped to errors you can implement an HttpReads[T]. This can be defined implicitly and will be used by http-verbs when making a request of type T:

```scala
val responseHandler = new HttpReads[Option[DelegationData]] {
 override def read(method: String, url: String, response: HttpResponse): Option[DelegationData] = {
   response.status match {
     case 200 => Try(response.json.as[DelegationData]) match {
       case Success(data) => Some(data)
       case Failure(e) => throw new RuntimeException("Unable to parse response")
     }
     case 404 => None
     case unexpectedStatus => throw new RuntimeException(s"Unexpected response code '$unexpectedStatus'")
   }
 }
}

def getDelegationData(oid: String, responseHandler: HttpReads[Option[DelegationData]] = responseHandler)(implicit hc: HeaderCarrier): Future[Option[DelegationData]] = {
 http.GET[Option[DelegationData]](delegationUrl(oid))
}
```

For more detailed examples have a look at [this tests](https://github.com/hmrc/http-verbs-example/blob/master/src/test/scala/uk/gov/hmrc/http)

### Default Error Handling

By default, HttpVerbs is opinionated in the way it handles response codes. Any status code other than a 200 will cause an exception to be thrown. 400 and 404 have their own specific exceptions. See below logic for more details

```scala
case status if is2xx(status) => response
case 400 => throw new BadRequestException(badRequestMessage(httpMethod, url, response.body))
case 404 => throw new NotFoundException(notFoundMessage(httpMethod, url, response.body))
case status if is4xx(status) => throw new Upstream4xxResponse(upstreamResponseMessage(httpMethod, url, status, response.body), status, 500, response.allHeaders)
case status if is5xx(status) => throw new Upstream5xxResponse(upstreamResponseMessage(httpMethod, url, status, response.body), status, 502)
case status => throw new Exception(s"$httpMethod to $url failed with status $status. Response body: '${response.body}'")
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
    
