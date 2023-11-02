package com.lsc
import NetGraphAlgebraDefs._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.graphx._
object SimRank {
  val config: Config = ConfigFactory.load()
  private val depth = config.getInt("Spark-project.sim-rank.depth")
  private val C = config.getInt("Spark-project.sim-rank.C")
  // SimRank algorithm
  def simRankAlgorithm(broadcastNeighbors1: Broadcast[scala.collection.Map[VertexId, Array[(VertexId, NodeObject)]]], broadcastNeighbors2: Broadcast[scala.collection.Map[VertexId, Array[(VertexId, NodeObject)]]], node1: VertexId, node2: VertexId, depth: Int = depth): Double = {

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
