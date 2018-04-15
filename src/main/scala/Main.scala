import io.circe.generic.auto._
import io.circe.parser._
import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.{Event, MouseEvent, window}
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.forceModule.Force

import scala.scalajs.js

/*
   Class representing data from JSON
  */
case class JsonObject(nodes: List[NodeD3], links: List[LinkfromJson])

case class LinkfromJson(source: String, target: String)

/*
   D3 specifics Class
  */
case class LinkD3(source: NodeD3, target: NodeD3) extends org.singlespaced.d3js.Link[NodeD3]

case class NodeD3(id: String, name: String, temp: Int) extends org.singlespaced.d3js.forceModule.Node

/*
  UI Class
 */
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

case class D3Graph(targetDivID: String, width: Int, height: Int, tooltip: Tooltip) {

  private val force: Force[NodeD3, LinkD3] = d3.layout.force()

  private var d3Nodes: js.Array[NodeD3] = force.nodes()
  private var d3Links: js.Array[LinkD3] = force.links()

  private val diameterRamp = d3.scale.linear().domain(js.Array(-20, 40)).range(js.Array(1, 20))
  private val colorRamp = d3.scale.quantile().domain(js.Array(15, 20, 30)).range(js.Array("#5d8fff", "#8dffc5", "#ff8e03"))


  private val svg = d3.select(s"#$targetDivID")
    .append("svg:svg")
    .attr("width", width)
    .attr("height", height)
    .attr("id", "svg")
    .attr("pointer-events", "all")
    .attr("viewBox", "0 0 " + width + " " + height)
    .attr("perserveAspectRatio", "xMinYMid")
    .append("svg:g")

  def newData(json: String): Unit = {
    println("Update nodes ...")
    val (newNode, newLink) = convertData(parseJson(json))
    d3Nodes = newNode
    d3Links = filterLinks(newLink, d3Nodes)

    update()
  }

  def addNode(node: NodeD3) {
    println("Add node ...")

    d3Nodes = d3Nodes :+ NodeD3(node.id, node.name, node.temp)
    println(s"number of nodes after addNode ${d3Nodes.length}")
    update()
  }

  def removeNode(id: String): Unit = {
    println("remove node ...")

    d3Nodes = d3Nodes.filterNot(_.id == id)
    d3Links = d3Links.filterNot(link => link.source.id == id || link.target.id == id)

    update()
  }

  def removeAllLinks(): Unit = {
    println("remove all links ...")
    d3Links = js.Array[LinkD3]()
    update()
  }

  def removeAllNodes(): Unit = {
    println("remove all nodes ...")
    d3Nodes = js.Array[NodeD3]()
    update()
  }

  def addLink(link: LinkfromJson): Unit = {
    val sourceOpt = d3Nodes.find(_.id == link.source)
    val targetOpt = d3Nodes.find(_.id == link.target)

    (sourceOpt, targetOpt) match {
      case (Some(source), Some(target)) => d3Links = d3Links :+ LinkD3(source, target)
      case (None, Some(_)) => System.err.println(s"addLink($link): source ${link.source} not found in the nodes")
      case (Some(_), None) => System.err.println(s"addLink($link): target ${link.target} not found in the nodes")
      case _ => System.err.println(s"addLink($link): Neither source ${link.source} and target ${link.target} has been found in the nodes")
    }
    update()
  }


  private def parseJson(json: String): JsonObject = {
    decode[JsonObject](json).right.getOrElse(JsonObject(List.empty[NodeD3], List.empty[LinkfromJson]))
  }

  private def filterLinks(allLinks: js.Array[LinkD3], curNodes: js.Array[NodeD3]) = {
    allLinks.filter(l => curNodes.contains(l.source) && curNodes.contains(l.target))
  }

  private def update(): Unit = {

    val link = updateLink()

    val node = updateNodes()

    def forceTick(e: Event) = {
      node.attr("transform", (d: NodeD3) => {
        "translate(" + d.x + "," + d.y + ")"
      })

      link
        .attr("x1", (d: LinkD3) => d.source.x)
        .attr("y1", (d: LinkD3) => d.source.y)
        .attr("x2", (d: LinkD3) => d.target.x)
        .attr("y2", (d: LinkD3) => d.target.y)
    }

    force
      .links(d3Links).nodes(d3Nodes)
      .size(js.Tuple2(width, height)).on("tick", forceTick)
      .charge(-8000)
      .linkDistance(120)
      .gravity(0.2)
      .friction(0.1)
      .start()
    keepNodesOnTop()
  }

  private def updateLink() = {
    val link = svg.selectAll("line")
      .data(d3Links, (d: LinkD3) => s"#${d.source.id}_#${d.target.id}")

    println(s"number of links in the updatesLinks ${d3Links.length}")

    link.enter().append("line")
      .attr("class", "link")
      .attr("stroke", "#ddd")
      .attr("stroke-opacity", 0.8)
      .attr("x1", (d: LinkD3) => d.source.x)
      .attr("y1", (d: LinkD3) => d.source.y)
      .attr("x2", (d: LinkD3) => d.target.x)
      .attr("y2", (d: LinkD3) => d.target.y)

    link.append("title")
      .text((d: LinkD3) => {
        d.source.id
      })

    link.exit().remove()
    link
  }

  private def updateNodes() = {
    val node = svg.selectAll("g.node")
      .data(d3Nodes, (d: NodeD3, _: Int) => d.id)

    println(s"number of nodes in the updatesNodes ${d3Nodes.length}")
    val nodeEnter = node.enter().append("g")
      .attr("class", "node")
      .call(force.drag)

    nodeEnter.append("svg:circle")
      .attr("class", "nodeCircle")
      .attr("cx", (d: NodeD3) => d.x)
      .attr("cy", (d: NodeD3) => d.y)
      .attr("r", (d: NodeD3) => diameterRamp(d.temp))
      .attr("fill", (d: NodeD3) => if (d.id.equalsIgnoreCase("smartLab_box")) {
        "red"
      } else {
        val rgb = colorRamp(d.temp)
        println(rgb)
        rgb
      })
      .style("stroke-width", 5.0)

    nodeEnter.append("svg:text")
      .attr("class", "textClass")
      .attr("text-anchor", "middle")
      .attr("dy", ".35em") // set offset y position
      .text((d: NodeD3) => s"${d.temp}")

    node.on("click", (_: NodeD3) => tooltip.hideTooltip()).call(force.drag)
    node.on("mouseover", showDetails)
      .on("mouseout", hideDetails)

    node.exit().remove()
    node
  }

  private def convertData(fromJson: JsonObject): (js.Array[NodeD3], js.Array[LinkD3]) = {
    import js.JSConverters._
    val nodes: js.Array[NodeD3] = fromJson.nodes.toJSArray
    val nodesById = nodes.groupBy(_.id)
    val links: js.Array[LinkD3] = fromJson.links.foldLeft(js.Array[LinkD3]())((array, node) => {
      val source = nodesById(node.source).head
      val target = nodesById(node.target).head
      array :+ LinkD3(source, target)
    })
    (nodes, links)
  }

  private def showDetails(node: NodeD3): Unit = {
    val content = "<p class='main'>" + node.name + "</span></p>" +
      "<hr class='tooltip-hr'>" +
      "<p class='main'>" + node.temp + " °C</span></p>"

    tooltip.showTooltip(content, d3.event.asInstanceOf[dom.Event])

  }

  private def hideDetails(node: NodeD3): Unit = {
    tooltip.hideTooltip()
  }

  private def keepNodesOnTop() {
    $(".nodeCircle").foreach(index => {
      val gnode = index.parentNode
      gnode.parentNode.appendChild(gnode)
    })
  }

}

object Main {

  def main(args: Array[String]): Unit = {
    val graph = D3Graph("svgDiv", 960, 600, Tooltip("svg-tooltip", 230))
    val json: String =
      """{
        |  "nodes": [
        |    {
        |      "name": "smartLab box",
        |      "id": "smartLab_box",
        |      "temp": 20
        |    },
        |    {
        |      "name": "Temperature 1",
        |      "id": "temp1",
        |      "temp": 16
        |    },
        |    {
        |      "name": "Temperature 2",
        |      "id": "temp2",
        |      "temp": 14
        |    },
        |    {
        |      "name": "Temperature 3",
        |      "id": "temp3",
        |      "temp": 23
        |    },
        |    {
        |      "name": "Temperature 4",
        |      "id": "temp4",
        |      "temp": 26
        |    },
        |    {
        |      "name": "Temperature 5",
        |      "id": "temp5",
        |      "temp": 30
        |    },
        |    {
        |      "name": "Temperature 6",
        |      "id": "temp6",
        |      "temp": 10
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

    val json2Level: String =
      """{
        |  "nodes": [
        |    {
        |      "name": "smartLab box",
        |      "id": "smartLab_box",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 1",
        |      "id": "temp1",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 2",
        |      "id": "temp2",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 3",
        |      "id": "temp3",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 4",
        |      "id": "temp4",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 5",
        |      "id": "temp5",
        |      "temp": 21
        |    },
        |    {
        |      "name": "Temperature 6",
        |      "id": "temp6",
        |      "temp": 21
        |    },
        |    {
        |      "name": "State",
        |      "id": "state_temp6",
        |      "temp": 21
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
        |    ,
        |    {
        |      "source": "temp6",
        |      "target": "state_temp6"
        |    }
        |    ]
        |}""".stripMargin


    graph.newData(json)

    val initialTimer = 3000
    val increments = 1000

    val it = (0 to 20).map(initialTimer + _ * increments).toIterator

    js.timers.setTimeout(it.next()) {
      graph.addNode(NodeD3("new1", "the new Node", 23))
      graph.addLink(LinkfromJson("temp4", "new1"))

    }

    js.timers.setTimeout(it.next()) {
      graph.addLink(LinkfromJson("temp4", "temp5"))
    }

    js.timers.setTimeout(it.next()) {
      graph.addLink(LinkfromJson("new1", "temp5"))
    }

    js.timers.setTimeout(it.next()) {
      graph.removeNode("new1")
    }

    js.timers.setTimeout(it.next()) {
      graph.removeAllLinks()
    }

    js.timers.setTimeout(it.next()) {
      graph.removeAllNodes()
    }

    js.timers.setTimeout(it.next()) {
      graph.newData(json2Level)
    }
  }
}
