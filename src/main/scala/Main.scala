import org.scalajs.dom
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js


case class SimpleNode(id: String, x: Double, y: Double)

case class SimpleLink(source: SimpleNode, target: SimpleNode) extends org.singlespaced.d3js.Link[SimpleNode]

object Main {

  def main(args: Array[String]): Unit = {

    val width = 640
    val height = 480

    println("Starting the main ...")

    val node1 = SimpleNode("1", width / 3, height / 2)
    val node2 = SimpleNode("2", 2 * width / 3, height / 2)

    val nodes: js.Array[SimpleNode] = js.Array(node1, node2)
    val links = js.Array(SimpleLink(node1, node2))


    var svg = d3.select("#svg").append("svg")
      .attr("width", width)
      .attr("height", height)

    val force = d3.layout.force()
      .size(js.Tuple2(width, height))
      .nodes(nodes)
      .links(links)
      .linkDistance(width / 2)

    val link = svg.selectAll(".link")
      .data(links)
      .enter().append("line")
      .attr("class", "link")

    val node: Selection[SimpleNode] = svg.selectAll(".node")
      .data(nodes)
      .enter().append("circle")
      .attr("class", "node")

    force.on("end", (e: dom.Event) => {
      node.attr("r", width / 25)
        .attr("cx", (d: SimpleNode) => d.x)
        .attr("cy", (d: SimpleNode) => d.y)

      link.attr("x1", (d: SimpleLink) => d.source.x)
        .attr("y1", (d: SimpleLink) => d.source.y)
        .attr("x2", (d: SimpleLink) => d.target.x)
        .attr("y2", (d: SimpleLink) => d.target.y)
      ()
    })
    println("about to start the force")
    force.start()
  }

}
