import NetGraphAlgebraDefs._
import ReadGraph.deserializeGraph
import org.apache.spark._
import org.apache.spark.graphx._

object Main {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName("GraphX Example")
      .setMaster("local[*]")
    val sc = new SparkContext(conf)
    val maybeGraph: Option[Graph[NodeObject, Action]] = deserializeGraph(s"input/${args(0)}", sc)
    val graph : Graph[NodeObject, Action] = maybeGraph.getOrElse {
      sys.exit(1)
    }
    val walkPath = RandomWalk.randomWalk(graph, startNodeId = 1, steps= 100)
    println("Completed walk path:")
    walkPath.foreach(nodeId => println(s"Visited node: $nodeId"))
    sc.stop()
  }
}