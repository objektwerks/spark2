package spark

import org.scalatest.{FunSuite, Matchers}

class DatasetTest extends FunSuite with Matchers {
  import SparkInstance._
  import sparkSession.implicits._

  val dataset = sparkSession.read.json("./data/json/person.json").as[Person].cache

  test("dataset") {
    dataset.count shouldBe 4

    val filterPersonByName = dataset.filter(_.name == "barney").as[Person]
    filterPersonByName.count shouldBe 1
    filterPersonByName.head.name shouldBe "barney"

    val filterPersonByAge = dataset.filter(_.age > 23).as[Person]
    filterPersonByAge.count shouldBe 1
    filterPersonByAge.head.age shouldBe 24

    val selectNameByAge = dataset.select("name").where("age == 24").as[String]
    selectNameByAge.head shouldBe "fred"

    val meanAge = dataset.agg(Map("age" -> "mean")).as[Double]
    meanAge.first shouldBe 22.5
  }

  test("dataframe") {
    dataset.createOrReplaceTempView("persons")
    val personsAsDataframe = dataset.toDF.cache
    personsAsDataframe.count shouldBe 4

    val minAgeAsRow = dataset.agg(Map("age" -> "min")).first
    minAgeAsRow.getLong(0) shouldBe 21

    val avgAgeAsRow = dataset.agg(Map("age" -> "avg")).first
    avgAgeAsRow.getDouble(0) shouldBe 22.5

    val maxAgeAsRow = dataset.agg(Map("age" -> "max")).first
    maxAgeAsRow.getLong(0) shouldBe 24
  }

  test("sql") {
    dataset.createOrReplaceTempView("persons")
    val persons = dataset.sqlContext.sql("select * from persons where age >= 21 and age <= 24 order by age").as[Person].cache
    persons.count shouldBe 4
    persons.head.name shouldBe "betty"
    persons.head.age shouldBe 21
  }
}