package spark

import org.apache.spark.sql.Row
import org.scalatest.{FunSuite, Matchers}

class DatasetTest extends FunSuite with Matchers {
  import SparkInstance._
  import sparkSession.implicits._

  test("dataset") {
    import Person._

    val dataset = sparkSession.read.json("./data/json/person.json").as[Person].cache
    dataset.count shouldBe 4

    val filterPersonByName = dataset.filter(_.name == "barney").cache
    filterPersonByName.count shouldBe 1
    filterPersonByName.head.name shouldBe "barney"

    val filterPersonByAge = dataset.filter(_.age > 23).cache
    filterPersonByAge.count shouldBe 1
    filterPersonByAge.head.age shouldBe 24

    val selectNameByAge = dataset.select("name").where("age == 24").as[String].cache
    selectNameByAge.count shouldBe 1
    selectNameByAge.head shouldBe "fred"

    dataset.map(_.age).collect.min shouldBe 21
    dataset.map(_.age).collect.avg shouldBe 22.5
    dataset.map(_.age).collect.max shouldBe 24
    dataset.map(_.age).collect.sum shouldBe 90

    dataset.agg(Map("age" -> "min")).first.getLong(0) shouldBe 21
    dataset.agg(Map("age" -> "avg")).first.getDouble(0) shouldBe 22.5
    dataset.agg(Map("age" -> "max")).first.getLong(0) shouldBe 24
    dataset.agg(Map("age" -> "sum")).first.getLong(0) shouldBe 90

    val groupPersonByRole = dataset.groupBy("role").avg("age").cache
    val mapOfPersonByRole = groupPersonByRole.collect.map(row => row.getString(0) -> row.getDouble(1)).toMap[String, Double]
    mapOfPersonByRole.size shouldBe 2
    mapOfPersonByRole("husband") shouldBe 23.0
    mapOfPersonByRole("wife") shouldBe 22.0
    groupPersonByRole.collect.map {
      case Row("husband", avgAge) => avgAge shouldBe 23.0
      case Row("wife", avgAge) => avgAge shouldBe 22.0
    }

    dataset.createOrReplaceTempView("persons")
    val persons = dataset.sqlContext.sql("select * from persons where age >= 21 and age <= 22 order by age").as[Person].cache
    persons.count shouldBe 2
    persons.head.name shouldBe "betty"
    persons.head.age shouldBe 21
  }
}