package spark

import java.net.InetAddress

import org.apache.spark.sql.SparkSession

object SparkInstance {
  val sparkEventLogDir = "/tmp/spark-events"
  val sparkEventDirCreated = createSparkEventsDir(sparkEventLogDir)
  println(s"*** $sparkEventLogDir exists or was created: $sparkEventDirCreated")

  val sparkSession = SparkSession
    .builder
    .master("local[*]")
    .appName(InetAddress.getLocalHost.getHostName)
    .config("spark.sql.shuffle.partitions", "4")
    .config("spark.sql.warehouse.dir", "./target/spark-warehouse")
    .config("spark.eventLog.enabled", true)
    .config("spark.eventLog.dir", sparkEventLogDir)
    .enableHiveSupport
    .getOrCreate()
  val sparkContext = sparkSession.sparkContext
  val sqlContext = sparkSession.sqlContext
  sparkContext.addSparkListener(SparkAppListener())
  sparkSession.streams.addListener(StreamingQueryAppListener())
  println("*** Initialized Spark instance.")

  sys.addShutdownHook {
    sparkSession.stop()
    println("*** Terminated Spark instance.")
  }
}