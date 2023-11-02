package com.lsc
import NetGraphAlgebraDefs._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, Graph, VertexId}

import java.io.{FileInputStream, ObjectInputStream}
import scala.util.{Try, Using}

object ReadGraph {


  def deserializeGraph(path: String, sc: SparkContext): Option[Graph[NodeObject, Action]]  = {
    val tryComponents: Try[List[NetGraphComponent]] = Using.Manager { use =>
      val fis = use(new FileInputStream(path))
      val ois = use(new ObjectInputStream(fis))
      ois.readObject().asInstanceOf[List[NetGraphComponent]]
    }
    tryComponents.toOption.flatMap { components =>
      val nodes: List[(VertexId, NodeObject)] = components.collect {
        case node: NodeObject => (node.id, node)
      }

      val edges: List[Edge[Action]] = components.collect {
        case action: Action => Edge(action.fromId, action.toId, action)
      }


      val vertexRDD = sc.parallelize(nodes)
      val edgeRDD = sc.parallelize(edges)

      Some(Graph(vertexRDD, edgeRDD)) // Constructing a GraphX graph
    }
  }
}
