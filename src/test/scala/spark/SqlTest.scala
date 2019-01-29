package spark

import org.apache.spark.sql.{Dataset, Row}
import org.scalatest.{FunSuite, Matchers}

class SqlTest extends FunSuite with Matchers {
  import SparkInstance._
  import sparkSession.implicits._

  test("dataframe sql") {
    val dataframe = sparkSession.read.json("./data/person/person.json").cache
    assert(dataframe.isInstanceOf[Dataset[Row]])
    dataframe.count shouldBe 4

    dataframe.createOrReplaceTempView("persons")

    val rows = sqlContext.sql("select * from persons where age >= 21 and age <= 22 order by age").cache
    rows.show
    rows.count shouldBe 2
    rows.head.getString(2) shouldBe "betty"
    rows.head.getLong(0) shouldBe 21

    sqlContext.sql("select min(age) from persons").take(1)(0).getLong(0) shouldBe 21
    sqlContext.sql("select avg(age) from persons").take(1)(0).getDouble(0) shouldBe 22.5
    sqlContext.sql("select max(age) from persons").take(1)(0).getLong(0) shouldBe 24
    sqlContext.sql("select sum(age) from persons").take(1)(0).getLong(0) shouldBe 90
  }

  test("dataset sql") {
    val dataset = sparkSession.read.json("./data/person/person.json").as[Person].cache
    dataset.count shouldBe 4

    dataset.createOrReplaceTempView("persons")

    val persons = sqlContext.sql("select * from persons where age >= 21 and age <= 22 order by age").as[Person].cache
    persons.count shouldBe 2
    persons.head.name shouldBe "betty"
    persons.head.age shouldBe 21

    sqlContext.sql("select min(age) from persons").as[Long].take(1)(0) shouldBe 21
    sqlContext.sql("select avg(age) from persons").as[Double].take(1)(0) shouldBe 22.5
    sqlContext.sql("select max(age) from persons").as[Long].take(1)(0) shouldBe 24
    sqlContext.sql("select sum(age) from persons").as[Long].take(1)(0) shouldBe 90
  }

  test("dataframe join") {
    val persons = sparkSession.read.json("./data/person/person.json").cache
    val tasks = sparkSession.read.json("./data/person/task.json").cache
    persons.count shouldBe 4
    tasks.count shouldBe 4

    persons.createOrReplaceTempView("persons")
    tasks.createOrReplaceTempView("tasks")

    val rows: Dataset[Row] = sqlContext.sql("SELECT * FROM persons, tasks WHERE persons.id = tasks.pid").cache
    rows.count shouldBe 4
    rows.show
  }

  test("dataset join") {
    val persons = sparkSession.read.json("./data/person/person.json").as[Person].cache
    val tasks = sparkSession.read.json("./data/person/task.json").as[Task].cache
    persons.count shouldBe 4
    tasks.count shouldBe 4

    persons.createOrReplaceTempView("persons")
    tasks.createOrReplaceTempView("tasks")

    val personsTasks: Dataset[PersonsTasks] = sqlContext.sql("SELECT * FROM persons, tasks WHERE persons.id = tasks.pid").as[PersonsTasks].cache
    personsTasks.count shouldBe 4
    personsTasks.show
  }
}