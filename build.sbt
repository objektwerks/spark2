name := "spark"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.15"
libraryDependencies ++= {
  val sparkVersion = "2.4.8"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-hive" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark" %% "spark-graphx" % sparkVersion,
    "io.delta" %% "delta-core" % "1.0.0",
    "org.scalikejdbc" %% "scalikejdbc" % "3.5.0", // Can't upgrade to 4.0.0
    "com.h2database" % "h2" % "2.0.210",
    "org.slf4j" % "slf4j-api" % "1.7.32",
    "org.scalatest" %% "scalatest" % "3.2.10" % Test
  )
}
