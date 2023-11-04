package com.lsc
import com.lsc.SimRank.simRankAlgorithm
import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.util.Random
/** Object that provides functionality to perform random walks on graphs.
 *
 * This object includes methods to execute random walks starting from a set of nodes
 * and uses the SimRank algorithm to compute similarities between nodes visited during the walk.
 */
object RandomWalk {
  /** Performs a random walk on the graph and computes similarities using SimRank.
   *
   * Starting from a set of given nodes, it performs a random walk for a given number of steps.
   * During the walk, it computes the similarity of each visited node to a set of comparison nodes
   * using the SimRank algorithm.
   *
   * @param ngraph The graph representing the normal state without perturbations.
   * @param graph The graph representing the perturbed state or the state to compare against.
   * @param startNodes An array of starting nodes (VertexId) and their associated data (CustomNode).
   * @param steps The number of steps to walk from each starting node.
   * @param sc The SparkContext used for various Spark operations.
   * @param comparisonSetRDD An RDD of VertexIds to which the visited nodes are compared.
   * @return An RDD of tuples, each containing a VertexId and a Map of comparison VertexIds with their similarity scores.
   */
  def randomWalk(ngraph: Graph[CustomNode, CustomEdge], graph: Graph[CustomNode, CustomEdge], startNodes: Array[(VertexId, CustomNode)], steps: Int, sc: SparkContext, comparisonSetRDD: RDD[VertexId]): RDD[(VertexId, Map[VertexId, Double])] = {
    val tmp_neighbors = graph.collectNeighbors(EdgeDirection.Either)
    val neighbors = tmp_neighbors.collectAsMap()

    val broadcastNeighbors = sc.broadcast(neighbors)
    val broadcastComparisonSet = sc.broadcast(comparisonSetRDD.collect())

    val tmp_neighbors2 = ngraph.collectNeighbors(EdgeDirection.Either)
    val neighbors2 = tmp_neighbors2.collectAsMap()
    val broadcastNeighbors2 = sc.broadcast(neighbors2)

    def walk(currentNode: (VertexId, CustomNode), remainingSteps: Int, visitedNodes: Set[VertexId]): List[(VertexId, CustomNode)] = {
      if (remainingSteps == 0) {
        List(currentNode)
      } else {
        val currentNeighbors = broadcastNeighbors.value
          .getOrElse(currentNode._1, Array.empty)
          .filterNot(node => visitedNodes.contains(node._1))
        if (currentNeighbors.isEmpty) {
          List(currentNode)
        } else {
          val nextNode = currentNeighbors(Random.nextInt(currentNeighbors.length))
          currentNode :: walk(nextNode, remainingSteps - 1, visitedNodes + currentNode._1 + nextNode._1)
        }
      }
    }

    val walkerTasks = startNodes.map(node => (node, steps))
    sc.parallelize(walkerTasks).flatMap { case (node, s) =>
      val path = walk(node, s, Set(node._1))
      path.map { case (vertexId, _) =>
        val similarities = broadcastComparisonSet.value.map { comparisonVertexId =>
          (comparisonVertexId, simRankAlgorithm(broadcastNeighbors, broadcastNeighbors2, vertexId, comparisonVertexId))
        }.toMap
        (vertexId, similarities) // The key of the tuple is VertexId, value is the similarities Map
      }
    }
  }
}
