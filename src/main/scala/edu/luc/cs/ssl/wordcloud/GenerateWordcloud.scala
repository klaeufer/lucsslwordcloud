package edu.luc.cs.ssl.wordcloud

import org.mongodb.scala._

// https://www.scala-lang.org/api/current/
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GenerateWordcloud extends App {

  val MIN_LENGTH = 6
  val TOP_N = 200
  val ignoreSet = Set("digitized", "google")

  val mongoUri = sys.env("MONGODB_URI") // TODO obtain from Heroku
  val mongoClient = MongoClient(mongoUri)
  val databaseName = mongoUri.split("/").last
  assert(databaseName.size > 0)
  val database = mongoClient.getDatabase(databaseName)
  val collection = database.getCollection("tweets")

  val wordArrays = for {
    document <- collection.find()
    line = document("text").asString.getValue
  } yield for {
    word <- line.split("(?U)[\\W]+")
    if word.length >= MIN_LENGTH
    wordLC = word.toLowerCase
    if !ignoreSet.contains(wordLC)
  } yield {
    wordLC
  }

  val frequencyTable = wordArrays.foldLeft(Map.empty[String, Int]) {
    case (t, arr) =>
      arr.foldLeft(t) {
        case (m, w) =>
          m ++ Seq(w -> (m.getOrElse(w, 0) + 1))
      }
  }

  val sortedTable = frequencyTable.map { m =>
    m.toSeq.sortBy(_._2)(math.Ordering.Int.reverse).take(TOP_N)
  }

  sortedTable.subscribe(new Observer[Seq[(String, Int)]] {
    override def onNext(result: Seq[(String, Int)]): Unit = println("Result = " + result)
    override def onError(e: Throwable): Unit = println("Failed")
    override def onComplete(): Unit = println("Completed")
  })

  Await.ready(sortedTable.toFuture, 60.seconds)
}
