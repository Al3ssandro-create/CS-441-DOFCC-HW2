package com.lsc

import org.apache.spark.graphx.VertexId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers {

  "AttackResults.addResult" should "correctly add the results of two AttackResults instances" in {
    val result1 = Main.AttackResults(1, 2, 3)
    val result2 = Main.AttackResults(2, 1, 3)

    result1.addResult(result2)

    result1.failed shouldEqual 3
    result1.success shouldEqual 3
    result1.total shouldEqual 6
  }

  "Main.attack" should "return success when nodes are the same" in {
    val node: VertexId = 123
    val attackResult = Main.attack(node, node)

    attackResult.failed shouldEqual 0
    attackResult.success shouldEqual 1
    attackResult.total shouldEqual 1
  }

  it should "return failure when nodes are different" in {
    val node1: VertexId = 123
    val node2: VertexId = 456
    val attackResult = Main.attack(node1, node2)

    attackResult.failed shouldEqual 1
    attackResult.success shouldEqual 0
    attackResult.total shouldEqual 1
  }
}
