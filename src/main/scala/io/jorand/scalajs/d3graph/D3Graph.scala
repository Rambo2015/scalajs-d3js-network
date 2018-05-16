package io.jorand.scalajs.d3graph

import io.circe.generic.auto._
import io.circe.parser._
import io.jorand.scalajs.Tooltip
import org.scalajs.dom
import org.scalajs.dom.Event
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.d3
import org.singlespaced.d3js.forceModule.{Force, _}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

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

  private val linkG = svg.append("g").attr("id", "links")
  private val nodeG = svg.append("g").attr("id", "nodes")


  def newData(json: String): Unit = {
    println(s"new Data are coming ... $json")
    val (newNode, newLink) = convertData(parseJson(json))
    println(s"the nodes are $newNode")
    println(s"the links are $newLink")
    d3Nodes = newNode
    d3Links = filterLinks(newLink, d3Nodes)

    update()
  }

  def addNode(node: NodeD3) {
    println("Add node ...")

    for (i <- (0 to d3Nodes.length - 1)) {
      if (d3Nodes(i).id == node.id) {
        d3Nodes(i).nodeText = node.nodeText
        d3Nodes(i).name = node.name
        d3Nodes(i).circleClass = node.circleClass
        d3Nodes(i).tooltip = node.tooltip
        d3Nodes(i).value = node.value
      }
    }
    if (d3Nodes.isEmpty || d3Nodes.filter(_.id == node.id).length == 0)
      d3Nodes = d3Nodes :+ node

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

  def addLink(link: LinkD3Json): Unit = {
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


  private def parseJson(json: String): D3GraphData = {
    val graph = decode[D3GraphData](json)
    graph match {
      case Right(graph) => graph
      case Left(error) =>
        println(error)
        D3GraphData(List.empty[NodeD3], List.empty[LinkD3Json])
    }
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
      .charge(-5000)
      .linkDistance(60)
      .gravity(0.9)
      .friction(0.8)
      .start()
  }

  private def updateLink() = {
    val link = linkG.selectAll("line")
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

    val node = nodeG.selectAll(".node")
      .data(d3Nodes, (d: NodeD3, _: Int) => d.id)

    println(s"number of nodes in the updatesNodes ${d3Nodes.length}")
    val nodeEnter = node.enter().append("g")
      .attr("class", "node")
      .call(force.drag)

    //ENTER
    nodeEnter.append("svg:circle")
      .style("stroke-width", 5.0)
      .attr("cx", (d: NodeD3) => d.x)
      .attr("cy", (d: NodeD3) => d.y)

    nodeEnter.append("svg:text")
      .attr("class", "textClass")
      .attr("text-anchor", "right")




    // UPDATE
    node.select("text")
      //.attr("x", (d: NodeD3) => d.nodeText.length*2)
      .attr("dy", ".35em") // set offset y position
      .attr("dx", "1em") // set offset x position
      .text((d: NodeD3) => d.nodeText)

    node.select("circle")
      .attr("r", (d: NodeD3) => diameterRamp(d.value))
      .attr("class", (d: NodeD3) => s"nodeCircle ${d.circleClass}")


    node.on("click", (_: NodeD3) => tooltip.hideTooltip()).call(force.drag)
    node.on("mouseover", showDetails)
      .on("mouseout", hideDetails)

    // EXIT
    node.exit().remove()
    node
  }

  private def convertData(fromJson: D3GraphData): (js.Array[NodeD3], js.Array[LinkD3]) = {
    val nodes: js.Array[NodeD3] = fromJson.nodes.toJSArray
    println(s"nodes: $nodes")
    val nodesById = nodes.groupBy(_.id)
    println(s"nodes by ID: $nodesById")
    val links: js.Array[LinkD3] = fromJson.links.foldLeft(js.Array[LinkD3]())((array, node) => {
      val source = nodesById(node.source).head
      val target = nodesById(node.target).head
      array :+ LinkD3(source, target)
    })
    (nodes, links)
  }

  private def showDetails(node: NodeD3): Unit = {
    tooltip.showTooltip(node.tooltip, d3.event.asInstanceOf[dom.Event])
  }

  private def hideDetails(node: NodeD3): Unit = {
    tooltip.hideTooltip()
  }

}

/*
   Class representing data from JSON
  */
case class D3GraphData(nodes: List[NodeD3], links: List[LinkD3Json])

/*
   D3 specifics Class
  */
case class LinkD3(source: NodeD3, target: NodeD3) extends org.singlespaced.d3js.Link[NodeD3]

case class LinkD3Json(source: String, target: String)

case class NodeD3(id: String, var name: String, var value: Int, var tooltip: String = "", var nodeText: String = "node", var circleClass: String = "online") extends Node
