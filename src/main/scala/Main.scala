package com.lsc
import com.lsc.ReadGraph.deserializeGraph2
import com.typesafe.config.ConfigFactory
import org.apache.spark._
import org.apache.spark.graphx._
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}
/** Main application object for graph analysis.
 *
 * This object contains the main method for running the graph analysis application.
 * It is responsible for setting up the Spark environment, loading graphs, and performing
 * graph-related operations, such as random walks and attack simulations.
 */
object Main {
  /** Case class to hold the results of attack simulations.
   *
   * @param failed  the number of failed attacks
   * @param success the number of successful attacks
   * @param total   the total number of attacks
   */
  case class AttackResults(var failed: Int, var success: Int, var total: Int) {
    /** Adds the results of another `AttackResults` to this one.
     *
     * @param other another `AttackResults` object to add to this one
     */
    def addResult(other: AttackResults): Unit = {
      this.failed += other.failed
      this.success += other.success
      this.total += other.total
    }
  }

  /** Simulates an attack on a node.
   *
   * @param comparedNode the ID of the node being compared
   * @param node         the ID of the node being attacked
   * @return `AttackResults` with updated success or failure
   */
   def attack(comparedNode: VertexId, node: VertexId) = {
    if(comparedNode == node) {
      AttackResults(failed = 0, success = 1, total = 1)
    } else {
      AttackResults(failed = 1, success = 0, total = 1)
    }
  }

  /** Main method to run the application.
   *
   * @param args command-line arguments passed to the application
   */
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(getClass)
    val config = ConfigFactory.load("h2.conf")

    val appName = config.getString("Spark-project.main.appName")
    val master = config.getString("Spark-project.main.master")
    val numberOfWalkers = config.getInt("Spark-project.main.numberOfWalkers")
    val similarityThreshold = config.getDouble("Spark-project.main.similarityThreshold")
    val steps = config.getInt("Spark-project.main.steps")
    /**
    * de-comment lines below for running locally
    */
//    val conf = new SparkConf()
//      .setAppName(appName)
//      .setMaster(master)
//      val sc = new SparkContext(conf)
    /**
     * comment next 2 lines for running locally
     */
    val spark = SparkSession.builder.appName(appName).getOrCreate()
    val sc = spark.sparkContext
    /**
     * comment above lines for running locally
     */
    //logger.info(args(0))
    val maybeGraph: Option[Graph[CustomNode, CustomEdge]] = deserializeGraph2(args(0),sc)
    val graph : Graph[CustomNode, CustomEdge] = maybeGraph.getOrElse {
      logger.error("Error loading normal graph.")
      sc.stop()
      sys.exit(1)
    }
    val valuableNodes = GraphUtils.getValuableNodeIds(graph)


    val maybePertGraph : Option[Graph[CustomNode, CustomEdge]] = deserializeGraph2(args(1),sc)
    val pertGraph: Graph[CustomNode, CustomEdge] = maybePertGraph.getOrElse {
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