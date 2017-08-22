package spark

import org.scalatest.FunSuite

import scala.io.Source

class RatingsTest extends FunSuite {
  import SparkInstance._

  test("count") {
    // u.data = ( userid, movieid, rating, timestamp )
    val data = Source.fromInputStream(this.getClass.getResourceAsStream("/ratings/u.data")).getLines.toSeq
    val lines = sparkContext.makeRDD(data).cache
    val ratings = lines.map(line => line.split("\t")(2))
    val results = ratings.countByValue
    val sortedResults = results.toSeq.sortBy(_._1)
    sortedResults.foreach(println)
  }
}