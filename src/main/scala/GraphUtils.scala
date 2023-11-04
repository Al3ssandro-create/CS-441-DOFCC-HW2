package com.lsc
import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.rdd.RDD
/** Utility object for performing operations on graphs.
 *
 * This object contains methods for extracting specific nodes from graphs
 * and other graph-related utilities.
 */
object GraphUtils {
  /** Retrieves the IDs of nodes that have valuable data.
   *
   * This method filters the nodes of a graph to find those that have the `valuableData`
   * property set to true and returns their IDs.
   *
   * @param graph the graph from which to retrieve the valuable nodes
   * @return an RDD of VertexId containing the IDs of the valuable nodes
   */
  def getValuableNodeIds(graph: Graph[CustomNode, CustomEdge]): RDD[VertexId] = {
    // Filter out the nodes where valuableData is true and collect only their VertexIds
    val valuableNodeIds = graph.vertices.filter {
      case (_, node) => node.valuableData
    }.map(_._1)

    valuableNodeIds
  }
}

