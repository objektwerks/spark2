package spark

import org.apache.log4j.Logger
import org.apache.spark.executor.TaskMetrics
import org.apache.spark.scheduler._

import scala.collection.mutable.ListBuffer

class SparkAppListener extends SparkListener {
  val events = ListBuffer[String]()

  def log(): Unit = {
    val logger = Logger.getLogger("SparkAppListener")
    events foreach { event => logger.info(event) }
  }

  override def onJobEnd(end: SparkListenerJobEnd): Unit = events += s"*** Job end: ${end.jobResult}"

  override def onStageCompleted(completed: SparkListenerStageCompleted): Unit = events += s"*** Stage completed: ${completed.stageInfo.name}"

  override def onTaskEnd(end: SparkListenerTaskEnd): Unit = {
    events += s"*** Task end info: ${taskInfoToString(end.taskInfo)}"
    events += s"*** Task end metrics: ${taskMetricsToString(end.taskMetrics)}"
  }

  def taskInfoToString(taskInfo: TaskInfo): String = {
    val info = ListBuffer[String]()
    info += s"status: ${taskInfo.status} "
    info += s"duration: ${taskInfo.duration} "
    info.mkString
  }

  def taskMetricsToString(taskMetrics: TaskMetrics): String = {
    val info = ListBuffer[String]()
    info += s"executor run time: ${taskMetrics.executorRunTime} "
    info += s"peak execution memory: ${taskMetrics.peakExecutionMemory} "
    info += s"result size: ${taskMetrics.resultSize} "
    info.mkString
  }
}