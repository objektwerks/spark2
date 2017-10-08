package spark

import org.apache.log4j.Logger
import org.apache.spark.scheduler._

import scala.collection.mutable.ListBuffer

class SparkAppListener extends SparkListener {
  val events = ListBuffer[String]()

  def log(): Unit = {
    val logger = Logger.getLogger(this.getClass)
    events foreach { event => logger.info(event) }
  }

  override def onApplicationStart(start: SparkListenerApplicationStart): Unit = events += s"*** On app start: ${start.toString}"

  override def onApplicationEnd(end: SparkListenerApplicationEnd): Unit = events += s"*** On app end: ${end.toString}"

  override def onJobStart(start: SparkListenerJobStart): Unit = events += s"*** On job start: ${start.toString}"

  override def onJobEnd(end: SparkListenerJobEnd): Unit = events += s"*** On job end: ${end.toString}"

  override def onStageSubmitted(submitted: SparkListenerStageSubmitted): Unit = events += s"*** On stage submitted: ${submitted.toString}"

  override def onStageCompleted(completed: SparkListenerStageCompleted): Unit = events += s"*** On stage completed: ${completed.toString}"

  override def onExecutorMetricsUpdate(update: SparkListenerExecutorMetricsUpdate): Unit = events += s"*** On executor update: ${update.toString}"

  override def onExecutorAdded(added: SparkListenerExecutorAdded): Unit = events += s"*** On executor added: ${added.toString}"

  override def onExecutorRemoved(removed: SparkListenerExecutorRemoved): Unit = events += s"*** On executor removed: ${removed.toString}"

  override def onExecutorBlacklisted(blacklisted: SparkListenerExecutorBlacklisted): Unit = events += s"*** On executor blacklisted: ${blacklisted.toString}"

  override def onExecutorUnblacklisted(unblacklisted: SparkListenerExecutorUnblacklisted): Unit = events += s"*** On executor unblacklisted: ${unblacklisted.toString}"

  override def onBlockManagerAdded(added: SparkListenerBlockManagerAdded): Unit = events += s"*** On block manager added: ${added.toString}"

  override def onBlockManagerRemoved(removed: SparkListenerBlockManagerRemoved): Unit = events += s"*** On block manager removed: ${removed.toString}"

  override def onBlockUpdated(updated: SparkListenerBlockUpdated): Unit = events += s"*** On block updated: ${updated.toString}"

  override def onTaskStart(start: SparkListenerTaskStart) = events += s"*** On task start: ${start.toString}"

  override def onTaskEnd(end: SparkListenerTaskEnd) = events += s"*** On task end: ${end.toString}"

  override def onTaskGettingResult(result: SparkListenerTaskGettingResult): Unit = events += s"*** On task result: ${result.toString}"

  override def onUnpersistRDD(unpersist: SparkListenerUnpersistRDD): Unit = events += s"*** On rdd unpersisted: ${unpersist.toString}"

  override def onNodeBlacklisted(blacklisted: SparkListenerNodeBlacklisted): Unit = events += s"*** On node blacklisted: ${blacklisted.toString}"

  override def onNodeUnblacklisted(unblacklisted: SparkListenerNodeUnblacklisted): Unit = events += s"*** On node unblacklisted: ${unblacklisted.toString}"

  override def onEnvironmentUpdate(update: SparkListenerEnvironmentUpdate): Unit = events += s"*** On env update: ${update.toString}"

  override def onOtherEvent(event: SparkListenerEvent): Unit = events += s"*** On other event: ${event.toString}"
}