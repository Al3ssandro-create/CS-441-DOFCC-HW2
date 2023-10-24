import NetGraphAlgebraDefs.{NetGraphComponent, _}
import org.apache.spark.SparkContext

import java.io.{FileInputStream, ObjectInputStream}
import scala.util.{Try, Using}
import org.apache.spark.graphx.{Edge, Graph, VertexId}

import scala.reflect.ClassTag

object ReadGraph {

  // Assuming your NodeObject has an id of type Long, and Action has sourceId and destId of type Long.
  case class GraphXReadyNode(id: VertexId, node: NodeObject)
  case class GraphXReadyEdge(sourceId: VertexId, destId: VertexId, action: Action)

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

      implicit val nodeTag: ClassTag[NodeObject] = ClassTag(classOf[NodeObject])
      implicit val actionTag: ClassTag[Action] = ClassTag(classOf[Action])

      val vertexRDD = sc.parallelize(nodes)
      val edgeRDD = sc.parallelize(edges)

      Some(Graph(vertexRDD, edgeRDD)) // Constructing a GraphX graph
    }
  }
}
