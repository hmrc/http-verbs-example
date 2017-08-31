
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

The most generic way to use the verbs is to ask for an HttpResponse in return. This special case class allows access to the raw details of the request, including content and status code:
```scala
val response: Future[Option[DelegationData]] = client.GET[HttpResponse]("http://localhost/bank-holidays.html") map {
   response => 
      response.status match {
        case 200 => Try(response.json.as[DelegationData]) match {
          case Success(data) => Some(data)
          case Failure(e) => throw new RuntimeException("Unable to parse response", method, url, e)
        }
        case 404 => None
        case unexpectedStatus => throw RuntimeException(s"Unexpected response code '$unexpectedStatus'", method, url)
      }
}
```

The above logic can be encapsulated using an HttpReads[T], which can be definted implicitly and will be used by http-verbs when making a request of type T:

```scala
  val responseHandler = new HttpReads[Option[DelegationData]] {
    override def read(method: String, url: String, response: HttpResponse): Option[DelegationData] = {
      response.status match {
        case 200 => Try(response.json.as[DelegationData]) match {
          case Success(data) => Some(data)
          case Failure(e) => throw new RuntimeException("Unable to parse response", method, url, e)
        }
        case 404 => None
        case unexpectedStatus => throw  new RuntimeException(s"Unexpected response code '$unexpectedStatus'", method, url)
      }
    }
  }

  def getDelegationData(oid: String, responseHandler: HttpReads[Option[DelegationData]] = responseHandler)(implicit hc: HeaderCarrier): Future[Option[DelegationData]] = {
    http.GET[Option[DelegationData]](delegationUrl(oid))
  }
```

For more detailed examples have a look at [this tests](https://github.com/hmrc/http-verbs-example/blob/master/src/test/scala/uk/gov/hmrc/http)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
    
