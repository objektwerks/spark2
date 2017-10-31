package spark

import org.apache.log4j.Logger
import org.apache.spark.executor.TaskMetrics
import org.apache.spark.scheduler._

import scala.collection.mutable.ArrayBuffer

class SparkAppListener extends SparkListener {
  val events = ArrayBuffer[String]()

  def log(): Unit = {
    val logger = Logger.getLogger("SparkAppListener")
    events foreach { event => logger.info(event) }
  }

  override def onJobEnd(end: SparkListenerJobEnd): Unit = events += s"*** Job - ${end.jobResult}"

  override def onTaskEnd(end: SparkListenerTaskEnd): Unit = events += s"*** Task - ${taskEndToString(end.taskInfo, end.taskMetrics)}"

  def taskEndToString(taskInfo: TaskInfo, taskMetrics: TaskMetrics): String = {
    val info = ArrayBuffer[String]()
    info += s"status: ${taskInfo.status} "
    info += s"duration: ${taskInfo.duration} "
    info += s"time: ${taskMetrics.executorRunTime} "
    info += s"memory: ${taskMetrics.peakExecutionMemory} "
    info += s"size: ${taskMetrics.resultSize}"
    info.mkString
  }
}