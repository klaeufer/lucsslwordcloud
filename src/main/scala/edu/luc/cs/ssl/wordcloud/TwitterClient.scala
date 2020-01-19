package edu.luc.cs.ssl.wordcloud

// https://sttp.readthedocs.io/en/latest/
import org.mongodb.scala.MongoClient
import sttp.client._

// https://github.com/json4s/json4s
import org.json4s._
import org.json4s.native.JsonMethods._

// https://www.scala-lang.org/api/current/
import scala.util.Try

object TwitterClient extends App {

  implicit val backend = HttpURLConnectionBackend()
  implicit val formats = DefaultFormats

  {
    for {

      // Option monad:
      // x = e is a straight name binding
      // x <- e is a monadic binding; if None, we end up in "orElse" at the bottom

      mongoUri <- sys.env.get("MONGODB_URI")
      apiKey <- sys.env.get("TWITTER_API_KEY")
      apiSecret <- sys.env.get("TWITTER_API_SECRET")
      searchTerm <- Try(args(0)).toOption

      // curl -u "key:secret" --data 'grant_type=client_credentials' https://api.twitter.com/oauth2/token

      tokenRequest = basicRequest
        .auth.basic(apiKey, apiSecret)
        .body("grant_type" -> "client_credentials")
        .post(uri"https://api.twitter.com/oauth2/token")

      response = tokenRequest.send()
      body <- response.body.toOption
      accessToken = (parse(body) \ "access_token").extract[String]

      // curl -i "https://api.twitter.com/labs/1/tweets/search?query=loyola" -H "Authorization: Bearer ..."

      searchRequest = basicRequest
        .auth.bearer(accessToken)
        .get(uri"https://api.twitter.com/labs/1/tweets/search?query=$searchTerm")

      response = searchRequest.send()
      body <- response.body.toOption

      //      mongoClient = MongoClient(mongoUri)
      //      databaseName = mongoUri.split("/").last
      //      database = mongoClient.getDatabase(databaseName)

    } yield {

      // TODO consider starting new monad for the Mongo driver

      println(body)
      for {
        JObject(tweet) <- parse(body) \ "data"
        JField("id", id) <- tweet
        JField("text", text) <- tweet
      } {
        println(id.extract[String] + ": " + text.extract[String])
      }

      //      database.getCollection("tweets").countDocuments().subscribe((n: Long) => println(n))
    }
  } getOrElse {
    System.err.println("fail")
  }
}
