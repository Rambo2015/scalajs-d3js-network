import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.jorand.scalajs.d3graph.NodeD3
import org.singlespaced.d3js.forceModule.Node

import scala.scalajs.js.annotation.JSExportAll


case class Person(name: String)
// defined class Person

case class Greeting(salutation: String, person: String, exclamationMarks: Int)
// defined class Greeting

val greetJSON = Greeting("Hey", "Chris", 3).asJson

val back = decode[Greeting](greetJSON.spaces2)

@JSExportAll
trait NodeTest  {
  def parent: NodeTest
}
//case class NodeD3Test(id: String,  name: String,  value: Int, var tooltip: String = "", var nodeText: String = "node", var circleClass: String = "online") extends NodeTest
case class NodeD3(id: String, var name: String, var value: Int, var tooltip: String = "", var nodeText: String = "node", var circleClass: String = "online") extends Node



val nodeTest = NodeD3("theId", "name", 23)

val node = nodeTest
val jsonNode = node.asJson