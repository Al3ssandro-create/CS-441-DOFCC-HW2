package com.lsc
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD
import org.json4s._
import org.json4s.native.JsonMethods._
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source
/** Object responsible for reading and deserializing graph data.
 *
 * Provides functionality to deserialize a graph from a JSON representation into a GraphX Graph.
 */
object ReadGraph {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  /** Deserializes a graph from a given path into a GraphX Graph.
   *
   * This method attempts to read a graph structure from a JSON file located at the given path
   * and deserialize it into a GraphX Graph with CustomNode and CustomEdge types.
   *
   * @param path The path to the JSON file representing the graph.
   * @param sc   The SparkContext used for creating RDDs.
   * @return An Option containing the Graph if deserialization is successful, None otherwise.
   */
  def deserializeGraph2(path: String, sc: SparkContext): Option[Graph[CustomNode, CustomEdge]] = {
    implicit val formats: Formats = DefaultFormats

    val source = if (path.startsWith("http") || path.startsWith("https")) {
      Source.fromURL(path)
    } else {
      Source.fromFile(path)
    }
    try{
      val json = parse(source.mkString)
      source.close()

      json.extractOpt[CustomGraph] match {
        case Some(customGraph) =>
          val vertexRDD: RDD[(VertexId, CustomNode)] = sc.parallelize(customGraph.nodes.map(n => (n.id.toLong, n)))
          val edgeRDD: RDD[Edge[CustomEdge]] = sc.parallelize(customGraph.edges.map(e => Edge(e.fromId.toLong, e.toId.toLong, e)))

          Some(Graph(vertexRDD, edgeRDD))

        case None =>
          None
      }
    }catch{
      case e: Exception =>
        logger.error(e.getMessage)
        None
    }
  }
}

