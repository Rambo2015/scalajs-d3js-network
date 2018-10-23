import org.querki.jquery.$
import org.scalatest._

class D3GraphSpecs extends FunSpec {


  Main.setupGraph()

  describe("D3Graph") {
    it("should contain a div") {
      assert($("div").length == 1)
    }

  }

}
