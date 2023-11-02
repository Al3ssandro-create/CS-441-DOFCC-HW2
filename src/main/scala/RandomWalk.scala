package com.lsc
import NetGraphAlgebraDefs._
import com.lsc.SimRank.simRankAlgorithm
import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

import scala.util.Random

object RandomWalk {

  def randomWalk(ngraph: Graph[NodeObject, Action], graph: Graph[NodeObject, Action], startNodes: Array[(VertexId, NodeObject)], steps: Int, sc: SparkContext, comparisonSetRDD: RDD[VertexId]): RDD[(VertexId, Map[VertexId, Double])] = {
    val tmp_neighbors = graph.collectNeighbors(EdgeDirection.Either)
    val neighbors = tmp_neighbors.collectAsMap()

    val broadcastNeighbors = sc.broadcast(neighbors)
    val broadcastComparisonSet = sc.broadcast(comparisonSetRDD.collect())

    val tmp_neighbors2 = ngraph.collectNeighbors(EdgeDirection.Either)
    val neighbors2 = tmp_neighbors2.collectAsMap()
    val broadcastNeighbors2 = sc.broadcast(neighbors2)

    def walk(currentNode: (VertexId, NodeObject), remainingSteps: Int, visitedNodes: Set[VertexId]): List[(VertexId, NodeObject)] = {
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
