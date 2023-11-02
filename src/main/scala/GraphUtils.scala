package com.lsc
import NetGraphAlgebraDefs.{Action, NodeObject}
import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.rdd.RDD

object GraphUtils {
  def getValuableNodeIds(graph: Graph[NodeObject, Action]): RDD[VertexId] = {
    // Filter out the nodes where valuableData is true and collect only their VertexIds
    val valuableNodeIds = graph.vertices.filter {
      case (_, node) => node.valuableData
    }.map(_._1)

    valuableNodeIds
  }
}
