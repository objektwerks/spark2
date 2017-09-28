package spark

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, StreamingLinearRegressionWithSGD}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object LinearRegressionApp extends App {
  val sparkSession = SparkSession.builder.master("local[*]").appName("regression").getOrCreate()
  val sparkContext = sparkSession.sparkContext

  val streamingContext = new StreamingContext(sparkContext, batchDuration = Seconds(1))

  val regressionTrainingDStream = textFileToDStream("./data/txt/regression.txt", sparkContext, streamingContext)
  val regressionTestingDStream = textFileToDStream("./data/txt/regression.txt", sparkContext, streamingContext)

  val regressionTrainingData = regressionTrainingDStream.map(LabeledPoint.parse).cache()
  val regressionTestingData = regressionTestingDStream.map(LabeledPoint.parse)

  regressionTrainingData.print()

  val numFeatures = 1
  val model = new StreamingLinearRegressionWithSGD().setInitialWeights(Vectors.zeros(numFeatures))
  model.algorithm.setIntercept(true)

  model.trainOn(regressionTrainingData)
  model.predictOnValues(regressionTestingData.map(labeledPoint => (labeledPoint.label, labeledPoint.features))).print()

  streamingContext.start
  streamingContext.awaitTerminationOrTimeout(1000)
  streamingContext.stop(stopSparkContext = false, stopGracefully = true)

  sparkSession.stop
}