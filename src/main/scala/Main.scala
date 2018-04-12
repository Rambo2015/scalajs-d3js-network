import io.circe.generic.auto._
import io.circe.parser._
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.{Event, MouseEvent, window}
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3

import scala.scalajs.js

case class JsonObject(nodes: List[SimpleNode], links: List[LinkfromJson])

case class LinkfromJson(source: String, target: String)

case class SimpleNode(id: String, name: String, playcount: Int)

case class LinkD3(source: NodeD3, target: NodeD3) extends org.singlespaced.d3js.Link[NodeD3]

case class NodeD3(id: String, name: String, playcount: Int) extends org.singlespaced.d3js.forceModule.Node

case class Tooltip(tooltipId: String, width: Int) {
  $("body").append(s"<div class='tooltip' id='$tooltipId'></div>")

  $(s"#$tooltipId").css("width", width)

  hideTooltip()

  def hideTooltip() {
    $(s"#$tooltipId").hide()
  }

  def showTooltip(content: String, event: dom.Event) {
    $("#" + tooltipId).html(content)
    $("#" + tooltipId).show()

    updatePosition(event)
  }

  def updatePosition(event: dom.Event) {
    val ttid = "#" + tooltipId
    val xOffset = 20
    val yOffset = 10

    val toolTipW = $(ttid).width()
    val toolTipeH = $(ttid).height()
    val windowY = $(window).scrollTop()
    val windowX = $(window).scrollLeft()
    val curX = event.asInstanceOf[MouseEvent].pageX
    val curY = event.asInstanceOf[MouseEvent].pageY
    var ttleft = if (curX < $(window).width() / 2) curX - toolTipW - xOffset * 2 else curX + xOffset
    if (ttleft < windowX + xOffset) {
      ttleft = windowX + xOffset
    }
    var tttop = if ((curY - windowY + yOffset * 2 + toolTipeH) > $(window).height()) curY - toolTipeH - yOffset * 2 else curY + yOffset
    if (tttop < windowY + yOffset) {
      tttop = curY + yOffset
    }
    $(ttid).css("top", tttop + "px").css("left", ttleft + "px")
  }
}

object Main {

  val graph: String =
    """{
      |  "nodes": [
      |    {
      |      "name": "smartLab box",
      |      "id": "smartLab_box",
      |      "playcount": 661020
      |    },
      |    {
      |      "name": "Temperature 1",
      |      "id": "temp1",
      |      "playcount": 772823
      |    },
      |    {
      |      "name": "Temperature 2",
      |      "id": "temp2",
      |      "playcount": 772823
      |    },
      |    {
      |      "name": "Temperature 3",
      |      "id": "temp3",
      |      "playcount": 772823
      |    },
      |    {
      |      "name": "Temperature 4",
      |      "id": "temp4",
      |      "playcount": 772823
      |    },
      |    {
      |      "name": "Temperature 5",
      |      "id": "temp5",
      |      "playcount": 772823
      |    },
      |    {
      |      "name": "Temperature 6",
      |      "id": "temp6",
      |      "playcount": 772823
      |    }
      |    ],
      |    "links": [
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp1"
      |    },
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp2"
      |    },
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp3"
      |    },
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp4"
      |    },
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp5"
      |    },
      |    {
      |      "source": "smartLab_box",
      |      "target": "temp6"
      |    }
      |    ]
      |}""".stripMargin


  val tooltip = Tooltip("svg-tooltip", 230)

  def main(args: Array[String]): Unit = {

    val width = 960
    val height = 600

    println("Starting the main ...")


    val svg = d3.select("#svg").append("svg")
      .attr("width", width)
      .attr("height", height)

    val nodesList = decode[JsonObject](graph).right.get

    println(s"parsed nodelist is $nodesList")

    val nodes: js.Array[NodeD3] = nodesList.nodes.foldLeft(js.Array[NodeD3]())((array, node) => {
      val newNode = NodeD3(node.id, node.name, node.playcount)
      newNode.x = Math.floor(Math.random() * width)
      newNode.y = Math.floor(Math.random() * height)
      array :+ newNode
    })
    val nodesById = nodes.groupBy(_.id)
    val links: js.Array[LinkD3] = nodesList.links.foldLeft(js.Array[LinkD3]())((array, node) => {
      val source = nodesById(node.source).head
      val target = nodesById(node.target).head
      array :+ LinkD3(source, target)
    })

    val force = d3.layout.force()
      .size(js.Tuple2(width, height))
      .nodes(nodes)
      .links(links)
      .linkDistance(width / 3.05)

    val link = svg.selectAll(".link")
      .data(links)

    link.enter().append("line")
      .attr("class", "link")
      .attr("stroke", "#ddd")
      .attr("stroke-opacity", 0.8)
      .attr("x1", (d: LinkD3) => d.source.x)
      .attr("y1", (d: LinkD3) => d.source.y)
      .attr("x2", (d: LinkD3) => d.target.x)
      .attr("y2", (d: LinkD3) => d.target.y)
    link.exit().remove()

    val node = svg.selectAll(".node")
      .data(nodes)


    node.enter().append("circle")
      .attr("class", "node")
      .attr("cx", (d: NodeD3) => d.x)
      .attr("cy", (d: NodeD3) => d.y)
      .attr("r", (_: NodeD3) => 10)
      .attr("fill", (d: NodeD3) => if (d.id.equalsIgnoreCase("smartLab_box")) "red" else "blue")
      .style("stroke-width", 5.0)

    def showDetails(node: NodeD3): Unit = {
      val content = "<p class='main'>" + node.name + "</span></p>" +
        "<hr class='tooltip-hr'>" +
        "<p class='main'>" + node.playcount + "</span></p>"

      tooltip.showTooltip(content, d3.event.asInstanceOf[dom.Event])

    }

    def hideDetails(node: NodeD3): Unit = {
      tooltip.hideTooltip()
    }

    node.on("mouseover", showDetails)
      .on("mouseout", hideDetails)

    node.exit().remove()


    def forceTick(e: Event) = {
      node
        .attr("cx", (d: NodeD3) => d.x)
        .attr("cy", (d: NodeD3) => d.y)

      link
        .attr("x1", (d: LinkD3) => d.source.x)
        .attr("y1", (d: LinkD3) => d.source.y)
        .attr("x2", (d: LinkD3) => d.target.x)
        .attr("y2", (d: LinkD3) => d.target.y)
    }

    force.on("tick", forceTick)
      .charge(-2000)
      .linkDistance(50)

    force.start()
  }


}
