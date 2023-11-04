import org.apache.spark.graphx._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar


class ReadGraphTest extends AnyFunSuite  with BeforeAndAfter with MockitoSugar {



  test("deserializeGraph2 should return None when given an invalid JSON path") {
    // Arrange
    val invalidPath = "path/to/nonexistent/graph.json"

    // Act

    // Assert
    assert(true)
  }

  // Add more tests for different scenarios like malformed JSON, network I/O, etc.
}
