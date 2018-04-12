import org.scalajs.dom
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.{Selection, d3}


import io.circe.generic.auto._, io.circe.parser._


import scala.scalajs.js

case class JsonObject(nodes: List[SimpleNode], links: List[LinkfromJson])

case class LinkfromJson(source: String, target: String)

case class SimpleNode(id: String, x: Double, y: Double)

case class SimpleLink(source: SimpleNode, target: SimpleNode) extends org.singlespaced.d3js.Link[SimpleNode]



object Main {

  val graph = """{
                |    "nodes": [  { "id": "0", "x": 208.992345, "y": 273.053211 },
                |                { "id": "1", "x": 595.98896,  "y":  56.377057 },
                |                { "id": "2", "x": 319.568434, "y": 278.523637 },
                |                { "id": "3", "x": 214.494264, "y": 214.893585 },
                |                { "id": "4", "x": 482.664139, "y": 340.386773 },
                |                { "id": "5", "x":  84.078465, "y": 192.021902 },
                |                { "id": "6", "x": 196.952261, "y": 370.798667 },
                |                { "id": "7", "x": 107.358165, "y": 435.15643  },
                |                { "id": "8", "x": 401.168523, "y": 443.407779 },
                |                { "id": "9", "x": 508.368779, "y": 386.665811 },
                |                { "id": "10", "x": 355.93773,  "y": 460.158711 },
                |                { "id": "11", "x": 283.630624, "y":  87.898162 },
                |                { "id": "12", "x": 194.771218, "y": 436.366028 },
                |                { "id": "13", "x": 477.520013, "y": 337.547331 },
                |                { "id": "14", "x": 572.98129,  "y": 453.668459 },
                |                { "id": "15", "x": 106.717817, "y": 235.990363 },
                |                { "id": "16", "x": 265.064649, "y": 396.904945 },
                |                { "id": "17", "x": 452.719997, "y": 137.886092 }
                |            ],
                |    "links": [  { "target": "11", "source":  "0" },
                |                { "target":  "3", "source":  "0" },
                |                { "target": "10", "source":  "0" },
                |                { "target": "16", "source":  "0" },
                |                { "target":  "1", "source":  "0" },
                |                { "target":  "3", "source":  "0" },
                |                { "target":  "9", "source":  "0" },
                |                { "target":  "5", "source":  "0" },
                |                { "target": "11", "source":  "0" },
                |                { "target": "13", "source":  "0" },
                |                { "target": "16", "source":  "0" },
                |                { "target":  "3", "source":  "1" },
                |                { "target":  "9", "source":  "1" },
                |                { "target": "12", "source":  "1" },
                |                { "target":  "4", "source":  "2" },
                |                { "target":  "6", "source":  "2" },
                |                { "target":  "8", "source":  "2" },
                |                { "target": "13", "source":  "2" },
                |                { "target": "10", "source":  "3" },
                |                { "target": "16", "source":  "3" },
                |                { "target":  "9", "source":  "3" },
                |                { "target":  "7", "source":  "3" },
                |                { "target": "11", "source":  "5" },
                |                { "target": "13", "source":  "5" },
                |                { "target": "12", "source":  "5" },
                |                { "target":  "8", "source":  "6" },
                |                { "target": "13", "source":  "6" },
                |                { "target": "10", "source":  "7" },
                |                { "target": "11", "source":  "7" },
                |                { "target": "17", "source":  "8" },
                |                { "target": "13", "source":  "8" },
                |                { "target": "11", "source": "10" },
                |                { "target": "16", "source": "10" },
                |                { "target": "13", "source": "11" },
                |                { "target": "14", "source": "12" },
                |                { "target": "14", "source": "12" },
                |                { "target": "14", "source": "12" },
                |                { "target": "15", "source": "12" },
                |                { "target": "16", "source": "12" },
                |                { "target": "15", "source": "14" },
                |                { "target": "16", "source": "14" },
                |                { "target": "15", "source": "14" },
                |                { "target": "16", "source": "15" },
                |                { "target": "16", "source": "15" },
                |                { "target": "17", "source": "16" }
                |            ]
                |    }""".stripMargin



  def main(args: Array[String]): Unit = {

    val width = 960
    val height = 600

    println("Starting the main ...")


    var svg = d3.select("#svg").append("svg")
      .attr("width", width)
      .attr("height", height)

    val nodesList = decode[JsonObject](graph).right.get

    println(s"parsed nodelist is $nodesList")

    val nodes: js.Array[SimpleNode] = nodesList.nodes.foldLeft(js.Array[SimpleNode]())((array, node) => array :+ node)
    val nodesById = nodes.groupBy(_.id)
    val links: js.Array[SimpleLink] = nodesList.links.foldLeft(js.Array[SimpleLink]())((array, node) => {
      val source = nodesById(node.source).head
      val target = nodesById(node.target).head
      array :+ SimpleLink(source, target)
    })

    val force = d3.layout.force()
      .size(js.Tuple2(width, height))
      .nodes(nodes)
      .links(links)
      .linkDistance(width / 3.05)

    val link = svg.selectAll(".link")
      .data(links)
      .enter().append("line")
      .attr("class", "link")

    val node: Selection[SimpleNode] = svg.selectAll(".node")
      .data(nodes)
      .enter().append("circle")
      .attr("class", "node")

    force.on("end", (e: dom.Event) => {
      node.attr("r", width / 100)
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
