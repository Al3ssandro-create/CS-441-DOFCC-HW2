package com.lsc
import NetGraphAlgebraDefs._
import com.lsc.ReadGraph.deserializeGraph
import com.typesafe.config.ConfigFactory
import org.apache.spark._
import org.apache.spark.graphx._
import org.slf4j.{Logger, LoggerFactory}
object Main {
  case class AttackResults(var failed: Int, var success: Int, var total: Int) {
    def addResult(other: AttackResults): Unit = {
      this.failed += other.failed
      this.success += other.success
      this.total += other.total
    }
  }

  def attack(comparedNode: VertexId, node: VertexId) = {
    if(comparedNode == node) {
      AttackResults(failed = 0, success = 1, total = 1)
    } else {
      AttackResults(failed = 1, success = 0, total = 1)
    }
  }

  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(getClass)
    val config = ConfigFactory.load()

    val appName = config.getString("Spark-project.main.appName")
    val master = config.getString("Spark-project.main.master")
    val numberOfWalkers = config.getInt("Spark-project.main.numberOfWalkers")
    val similarityThreshold = config.getDouble("Spark-project.main.similarityThreshold")
    val steps = config.getInt("Spark-project.main.steps")

    val conf = new SparkConf()
      .setAppName(appName)
      .setMaster(master)
    val sc = new SparkContext(conf)
    val maybeGraph: Option[Graph[NodeObject, Action]] = deserializeGraph(s"${args(0)}", sc)
    val graph : Graph[NodeObject, Action] = maybeGraph.getOrElse {
      logger.error("Error loading normal graph.")
      sc.stop()
      sys.exit(1)
    }
    val valuableNodes = GraphUtils.getValuableNodeIds(graph)


    val maybePertGraph : Option[Graph[NodeObject, Action]] = deserializeGraph(s"${args(1)}", sc)
    val pertGraph: Graph[NodeObject, Action] = maybePertGraph.getOrElse {
      logger.error("Error loading perturbed graph.")
      sc.stop()
      sys.exit(1)
    }
    val randomSeed = System.currentTimeMillis
    val startingNodes = pertGraph.vertices.takeSample (withReplacement = true, num = numberOfWalkers, seed = randomSeed)
    if (startingNodes.isEmpty) {
      logger.error("No node in the graph.")
      sc.stop()
      sys.exit(1)
    }
    val result = AttackResults(0, 0, 0) //(FAILED,SUCCESS,TOTAL)
    // Unique seed for each partition
    val walksRDD = RandomWalk.randomWalk(graph, pertGraph, startingNodes, steps = steps, sc, valuableNodes)
    // To print the results:
    walksRDD.collect().foreach { case (node, similarities) =>
      logger.info(s"Results for node: $node")
      similarities.foreach { case (comparedNode, score) =>
        logger.info(s" - Compared to node: $comparedNode, similarity score: $score")
        if (score > similarityThreshold) {
          val tmp = attack(comparedNode, node) // Assuming 'attack' function is defined elsewhere
          result.addResult(tmp) // Assuming 'result' is an accumulator or a collection to store results
        }
      }
    }
    logger.info(s"FAILED ATTACKS: ${result.failed}")
    logger.info(s"SUCCESSFUL ATTACKS: ${result.success}")
    logger.info(s"TOTAL ATTACKS: ${result.total}")
    sc.stop()
  }
}