package spark

import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.scalatest.FunSuite

import scala.collection.mutable

class SparkTest extends FunSuite {
  val conf = SparkInstance.conf
  val context = SparkInstance.context
  val sqlContext = SparkInstance.sqlContext

  test("transformations with action") {
    val rdd = context.makeRDD(Array(1, 2, 3)).cache
    assert(rdd.filter(_ % 2 == 0).first == 2)
    assert(rdd.filter(_ % 2 != 0).first == 1)
    assert(rdd.map(_ + 1).sum == 9)
    assert(rdd.map(_ + 1).collect sameElements Array(2, 3, 4))
  }

  test("actions") {
    val rdd = context.makeRDD(Array(1, 2, 3)).cache
    assert(rdd.count == 3)
    assert(rdd.first == 1)
    assert(rdd.min == 1)
    assert(rdd.max == 3)
    assert(rdd.mean == 2.0)
    assert(rdd.variance == 0.6666666666666666)
    assert(rdd.sampleVariance == 1.0)
    assert(rdd.stdev == 0.816496580927726)
    assert(rdd.sampleStdev == 1.0)
    assert(rdd.sum == 6)
    assert(rdd.fold(0)(_ + _) == 6)
    assert(rdd.reduce(_ + _) == 6)
    assert(rdd.take(1) sameElements Array(1))
  }

  test("parallelize") {
    val data = 1 to 1000000
    val rdd = context.parallelize(data)
    val result = rdd.filter(_ % 2 == 0).collect
    assert(result.length == 500000)
  }

  test("partitioner") {
    val rdd = context.parallelize(List((1, 1), (2, 2), (3, 3))).partitionBy(new HashPartitioner(2)).persist
    val partitioner = rdd.partitioner.get // ShuffleRDDPartition @0 / @1
    assert(partitioner.numPartitions == 2)
  }

  test("aggregate") {
    val data = 1 to 10
    val rdd = context.parallelize(data)
    val (x, y) = rdd.aggregate((0, 0))((x, y) => (x._1 + y, x._2 + 1), (x, y) => (x._1 + y._1, x._2 + y._2))
    assert(x == 55 && y == 10)
  }

  test("sets") {
    val rdd1 = context.makeRDD(Array(1, 2, 3)).cache
    val rdd2 = context.makeRDD(Array(3, 4, 5)).cache
    assert(rdd1.union(rdd2).collect sameElements Array(1, 2, 3, 3, 4, 5))
    assert(rdd1.intersection(rdd2).collect sameElements Array(3))
    assert(rdd1.subtract(rdd2).collect.sorted sameElements Array(1, 2))
    assert(rdd2.subtract(rdd1).collect.sorted sameElements Array(4, 5))

    val rdd3 = context.makeRDD(Array(1, 1, 2, 2, 3, 3))
    assert(rdd3.distinct.collect.sorted sameElements Array(1, 2, 3))

    val rdd4 = context.makeRDD(Array(1, 2))
    val rdd5 = context.makeRDD(Array(3, 4))
    assert(rdd4.cartesian(rdd5).collect sameElements Array((1,3), (1, 4), (2, 3), (2, 4)))
  }

  test("reduce by key") {
    val rdd = context.makeRDD(Array((1, 1), (1, 2), (1, 3))).cache
    val (key, aggregate) = rdd.reduceByKey(_ + _).first
    assert(rdd.keys.collect sameElements Array(1, 1, 1))
    assert(rdd.values.collect sameElements Array(1, 2, 3))
    assert(key == 1 && aggregate == 6)
  }

  test("group by key") {
    val rdd = context.makeRDD(Array((1, 1), (1, 2), (1, 3))).cache
    val (key, list) = rdd.groupByKey.first
    assert(rdd.keys.collect sameElements Array(1, 1, 1))
    assert(rdd.values.collect sameElements Array(1, 2, 3))
    assert(key == 1 && list.sum == 6)
  }

  test("sort by key") {
    val rdd = context.makeRDD(Array((3, 1), (2, 2), (1, 3)))
    assert(rdd.reduceByKey(_ + _).sortByKey(ascending = true).collect sameElements Array((1,3), (2, 2), (3, 1)))
  }

  test("map values") {
    val rdd = context.makeRDD(Array((1, 1), (1, 2), (1, 3)))
    val (key, values) = rdd.mapValues(_ * 2).groupByKey.first
    assert(key == 1 && values.sum == 12)
  }

  test("text") {
    val rdd = context.makeRDD(SparkInstance.license).cache
    val totalLines = rdd.count
    assert(totalLines == 19)

    val selectedWordCount = rdd.filter(_.contains("Permission")).count
    assert(selectedWordCount == 1)

    val longestLine = rdd.map(l => l.length).reduce((a, b) => Math.max(a, b))
    assert(longestLine == 77)

    val wordCountRdd = countWords(rdd).cache
    val totalWords = wordCountRdd.count
    assert(totalWords == 95)

    val maxCount = wordCountRdd.values.max
    val (word, count) = wordCountRdd.filter(_._2 == maxCount).first
    assert(word == "the" && count == 14)
  }

  test("dataframe") {
    val df = sqlContext.read.json(context.makeRDD(SparkInstance.personJson))

    val names = df.select("name").orderBy("name").collect
    assert(names.length == 4)
    assert(names.head.mkString == "barney")

    val ages = df.select("age").orderBy("age").collect
    assert(ages.length == 4)
    assert(ages.head.getLong(0) == 21)

    var row = df.filter(df("age") > 23).first
    assert(row.getLong(0) == 24)
    assert(row.getAs[String](1) == "fred")

    row = df.agg(Map("age" -> "max")).first
    assert(row.getLong(0) == 24)

    row = df.agg(Map("age" -> "avg")).first
    assert(row.getDouble(0) == 22.5)
  }

  test("json > case class > dataframe") {
    val rdd = sqlContext.read.json(context.makeRDD(SparkInstance.personJson)).map(p => Person(p(0).asInstanceOf[Long], p(1).asInstanceOf[String]))
    val df = sqlContext.createDataFrame[Person](rdd)
    df.registerTempTable("persons")

    val names = df.select("name").orderBy("name").collect
    assert(names.length == 4)
    assert(names.head.mkString == "barney")

    val ages = df.select("age").orderBy("age").collect
    assert(ages.length == 4)
    assert(ages.head.getLong(0) == 21)
  }

  test("aggregator > dataset") {
    import sqlContext.implicits._
    val sum = new Aggregator[Int, Int, Int] with Serializable {
      def zero: Int = 0
      def reduce(b: Int, a: Int) = b + a
      def merge(b1: Int, b2: Int) = b1 + b2
      def finish(b: Int) = b
    }.toColumn

    val ds = Seq(1, 2, 3).toDS()
    assert(ds.select(sum).collect.apply(0) == 6)
  }

  test("json > case class > dataset") {
    import sqlContext.implicits._
    val ds = sqlContext.read.json(context.makeRDD(SparkInstance.personJson)).as[Person]
    assert(ds.count == 4)
    assert(ds.filter(_.age == 24).first.name == "fred")
  }

  test("json > case class > aggregator > dataset") {
    import sqlContext.implicits._
    val sum =  new Aggregator[Data, Long, Long] {
      def zero: Long = 0
      def reduce(b: Long, a: Data): Long = b + a.n
      def merge(b1: Long, b2: Long): Long = b1 + b2
      def finish(b: Long): Long = b
    }.toColumn

    val ds = sqlContext.read.json(context.makeRDD(SparkInstance.dataJson)).as[Data]
    assert(ds.select(sum).collect.apply(0) == 6)
  }

  test("stateless spark streaming") {
    val streamingContext = new StreamingContext(context, Milliseconds(1000))
    val queue = mutable.Queue[RDD[String]]()
    val ds = streamingContext.queueStream(queue)
    queue += context.makeRDD(SparkInstance.license)
    val wordCountDs = countWords(ds)
    wordCountDs.saveAsTextFiles("./target/output/test/ds")
    streamingContext.start
    streamingContext.awaitTerminationOrTimeout(1000)
    streamingContext.stop(stopSparkContext = false, stopGracefully = true)
  }
}