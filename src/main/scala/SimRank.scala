package com.lsc
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.graphx._
/** Object containing the implementation of the SimRank algorithm for similarity computation.
 *
 * SimRank is a graph-based similarity measure. This implementation calculates the similarity between two nodes
 * in a graph based on the similarity of their corresponding neighbors.
 */
object SimRank {
  val config: Config = ConfigFactory.load("h2.conf")
  private val depth = config.getInt("Spark-project.sim-rank.depth")
  private val C = config.getInt("Spark-project.sim-rank.C")

  /** Computes the SimRank similarity between two nodes.
   *
   * @param broadcastNeighbors1 Broadcast variable containing the neighbors map of the first graph.
   * @param broadcastNeighbors2 Broadcast variable containing the neighbors map of the second graph.
   * @param node1               The ID of the first node.
   * @param node2               The ID of the second node.
   * @param depth               The maximum depth of recursion, which limits the computational complexity.
   * @return The similarity score between the two nodes, ranging from 0 to 1.
   */
  def simRankAlgorithm(broadcastNeighbors1: Broadcast[scala.collection.Map[VertexId, Array[(VertexId, CustomNode)]]], broadcastNeighbors2: Broadcast[scala.collection.Map[VertexId, Array[(VertexId, CustomNode)]]], node1: VertexId, node2: VertexId, depth: Int = depth): Double = {

    if (node1 == node2) return 1.0  // Similarity of a node to itself is 1

    if (depth == 0) return 0.0  // Depth limits the recursive calls to make the computation feasible

    //val neighborsNode1 = graph.collectNeighbors(EdgeDirection.In).lookup(node1).headOption.getOrElse(Array.empty).map(_._1)
    val neighborsNode1 = broadcastNeighbors1.value.getOrElse(node1, Array.empty).map(_._1)
    val neighborsNode2 = broadcastNeighbors2.value.getOrElse(node2, Array.empty).map(_._1)

    if (neighborsNode1.isEmpty || neighborsNode2.isEmpty) return 0.0  // If either node has no neighbors, similarity is 0

    val sumOfSimilarities = for {
      i <- neighborsNode1
      j <- neighborsNode2
    } yield simRankAlgorithm(broadcastNeighbors1,broadcastNeighbors2, i, j, depth - 1)

    C / (neighborsNode1.length * neighborsNode2.length) * sumOfSimilarities.sum
  }
}
