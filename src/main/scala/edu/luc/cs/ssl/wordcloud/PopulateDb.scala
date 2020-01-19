package edu.luc.cs.ssl.wordcloud

import com.mongodb.ConnectionString
import org.mongodb.scala.{ MongoClient, MongoClientSettings }
import org.mongodb.scala.bson.collection.immutable.Document

// https://www.scala-lang.org/api/current/
import scala.io.Source
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object PopulateDb extends App {

  val mongoUri = sys.env("MONGODB_URI") // TODO obtain from Heroku
  val settings = MongoClientSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUri))
    .retryWrites(false)
    .build()
  val mongoClient = MongoClient(settings)
  val databaseName = mongoUri.split("/").last
  assert { databaseName.size > 0 }
  val database = mongoClient.getDatabase(databaseName)
  val collection = database.getCollection("tweets")

  val documents = Source.stdin.getLines.zip(Iterator.from(1)).map {
    case (str, i) =>
      Document("_id" -> i, "text" -> str)
  }.toSeq

  println(f"attempting to insert ${documents.size} documents")

  val job = for {
    r1 <- collection.insertMany(documents)
    r2 <- collection.countDocuments()
  } yield {
    println(f"${r2} documents inserted")
  }

  Await.ready(job.toFuture, 30.seconds)
}
