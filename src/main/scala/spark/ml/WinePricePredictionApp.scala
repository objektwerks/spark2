package spark.ml

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.GBTRegressor
import spark.SparkInstance

object WinePricePredictionApp extends App {
  import SparkInstance._
  import Wine._

  // Load data files into dataframe.
  val dataframe = sparkSession
    .read
    .format("csv")
    .option("header", "true")
    .schema(wineSchema)
    .load("./data/wine/*.csv")
    .na
    .drop()

  // Split dataframe into training and test datasets.
  val Array(trainingData, testData) = dataframe.randomSplit(Array(0.8, 0.2))

  // Create country indexer.
  val countryIndexer = new StringIndexer()
    .setInputCol("country")
    .setOutputCol("countryIndex")

  // Create points and country index features - or attributes.
  val featuresAssembler = new VectorAssembler()
    .setInputCols(Array("points", "countryIndex"))
    .setOutputCol("features")

  // Create label - or target value.
  val labelColumn = "price"

  // Create GBT regressor - or gradient-boosted tree estimator.
  val gradientBoostedTreeEstimator = new GBTRegressor()
    .setLabelCol(labelColumn)
    .setFeaturesCol("features")
    .setPredictionCol("Predicted " + labelColumn)
    .setMaxIter(100)

  // Create stages and pipeline.
  val stages = Array(countryIndexer, featuresAssembler, gradientBoostedTreeEstimator)
  val pipeline = new Pipeline().setStages(stages)

  // Create model via pipeline and training dataset.
  val model = pipeline.fit(trainingData)

  // Create predictions dataframe via model and test dataset.
  val predictions = model.transform(testData)
  predictions.show(10)

  // Create regression evaluator.
  val evaluator = new RegressionEvaluator()
    .setLabelCol(labelColumn)
    .setPredictionCol("predicted " + labelColumn)
    .setMetricName("rmse")

  // Evaluate predictions via regression evaluator.
  println(s"Regression Root Mean Squared Error: ${evaluator.evaluate(predictions)}")

  sparkListener.log()
  sparkSession.stop()
}