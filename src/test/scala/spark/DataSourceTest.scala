package spark

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, Row}
import org.scalatest.{FunSuite, Matchers}

class DataSourceTest extends FunSuite with Matchers {
  import SparkInstance._
  import sparkSession.implicits._

  test("csv") {
    val dataframe: Dataset[Row] = sparkSession.read.csv("./data/txt/friends.txt")
    dataframe.count shouldBe 500

    val uniqueNames: Dataset[String] = dataframe.map(row => row.getString(1)).distinct
    uniqueNames.count shouldBe 30
  }

  test("text") {
    val rdd: RDD[String] = sparkContext.textFile("./data/txt/license.txt")
    rdd.count shouldBe 19

    val dataframe: Dataset[Row] = sparkSession.read.text("./data/txt/license.txt")
    dataframe.count shouldBe 19

    val dataset: Dataset[String] = sparkSession.read.textFile("./data/txt/license.txt")
    dataset.count shouldBe 19
  }

  test("json") {
    val dataframe: Dataset[Row] = sparkSession.read.json("./data/json/person.json")
    dataframe.count shouldBe 4

    val dataset: Dataset[Person] = sparkSession.read.json("./data/json/person.json").as[Person]
    dataset.count shouldBe 4
  }

  test("parquet") {
    val dataset: Dataset[Person] = sparkSession.read.json("./data/json/person.json").as[Person]
    dataset.write.parquet("./target/person.parquet")

    val parquet: Dataset[Person] = dataset.sqlContext.read.parquet("./target/person.parquet").as[Person]
    parquet.createOrReplaceTempView("persons")

    val resultset: Dataset[Person] = parquet.sqlContext.sql("select * from persons where age >= 21 and age <= 22 order by age").as[Person].cache
    resultset.count shouldBe 2
    resultset.head.name shouldBe "betty"
    resultset.head.age shouldBe 21
  }
}