import NetGraphAlgebraDefs._
import org.apache.spark.graphx._

import scala.annotation.tailrec
import scala.util.Random

object RandomWalk {

  /**
   * Performs a random walk on a graph from a specified starting node, without visiting the same node twice.
   *
   * @param graph The graph to walk on.
   * @param startNodeId The ID of the starting node for the walk.
   * @param steps The number of steps of the walk.
   * @return A sequence of node IDs representing the path taken by the walk.
   */
  def randomWalk(graph: Graph[NodeObject, Action], startNodeId: VertexId, steps: Int): Seq[VertexId] = {
    // We'll use collectNeighborIds to get an Array of neighboring VertexIds (not the whole vertices)
    val neighbors: VertexRDD[Array[VertexId]] = graph.collectNeighborIds(EdgeDirection.Either)

    // Cache neighbors RDD as it will be used in every step of the walk
    neighbors.cache()

    // Define the recursive function inside the main function
    @tailrec
    def walk(currentNodeId: VertexId, remainingSteps: Int, visitedNodes: Set[VertexId], path: Seq[VertexId]): Seq[VertexId] = {
      if (remainingSteps == 0) {
        path // Return the path if no steps remaining
      } else {
        val currentNeighbors = neighbors.lookup(currentNodeId)
          .headOption
          .getOrElse(Array.empty)
          .filterNot(visitedNodes.contains) // filter out already visited nodes

        if (currentNeighbors.isEmpty) {
          path // If no unvisited neighbors, return the path
        } else {
          val nextNodeIdx = Random.nextInt(currentNeighbors.length)
          val nextNodeId = currentNeighbors(nextNodeIdx)
          /**
          *
           **/
          walk(nextNodeId, remainingSteps - 1, visitedNodes + nextNodeId, path :+ nextNodeId)
          // Recurse with the next node, decrementing the remaining steps and adding the next node to the visited set and path
        }
      }
    }

    // Initiate the recursive walk with the starting node, steps, initial visited nodes set, and path
    walk(startNodeId, steps, Set(startNodeId), Seq(startNodeId))
  }
}

// Usage
// val graph: Graph[NodeObject, Action] = // ... your graph initialization
// val walkPath = RandomWalk.randomWalk(graph, startNodeId = 1, steps = 100)
// walkPath.foreach(println)
