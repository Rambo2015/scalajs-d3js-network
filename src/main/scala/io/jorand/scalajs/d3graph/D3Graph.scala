package io.jorand.scalajs.d3graph

import io.circe.generic.auto._
import io.circe.parser._
import io.jorand.scalajs.Tooltip
import io.jorand.scalajs.d3graph.D3Graph.{Circle, Rect, Shape}
import org.scalajs.dom
import org.scalajs.dom.Event
import org.singlespaced.d3js
import org.singlespaced.d3js.Ops._
import org.singlespaced.d3js.forceModule.{Force, _}
import org.singlespaced.d3js.selection.Update
import org.singlespaced.d3js.{Selection, d3}

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object D3Graph {

  sealed trait Shape {
    def name: String

    def className: String
  }

  case object Rect extends Shape {
    val name = "rect"
    val className = "nodeRect"
  }

  case object Circle extends Shape {
    val name = "circle"
    val className = "nodeCircle"
  }

}

case class D3Graph(targetDivID: String, width: Int, height: Int, tooltip: Tooltip) {

  private val force: Force[NodeD3, LinkD3] = d3.layout.force()

  private var d3Nodes: js.Array[NodeD3] = force.nodes()
  private var d3Links: js.Array[LinkD3] = force.links()

  private val diameterRamp = d3.scale.linear().domain(js.Array(-20, 40)).range(js.Array(1, 20))
  private val squareRamp = d3.scale.linear().domain(js.Array(-20, 40)).range(js.Array(1, 46))

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
  private val refsG = svg.append("g").attr("id", "defs").append("defs")


  def newData(json: String): Unit = {
    //    println(s"new Data are coming ... $json")
    val (newNode, newLink) = convertData(parseJson(json))
    //    println(s"the nodes are $newNode")
    //    println(s"the links are $newLink")
    d3Nodes = newNode
    d3Links = filterLinks(newLink, d3Nodes)

    update()
  }

  def markRootNode(clazz: String): Unit = {
    for (i <- 0 until d3Nodes.length) {
      if (!d3Links.exists(_.target == d3Nodes(i))) {
        d3Nodes(i).statusClass = clazz
      }
    }
  }

  def markLeafNode(clazz: String): Unit = {
    for (i <- 0 until d3Nodes.length) {
      if (!d3Links.exists(_.source == d3Nodes(i))) {
        d3Nodes(i).statusClass = clazz
      }
    }
  }

  def addNode(node: NodeD3, needUpdate: Boolean = true) {
    //    println("Add node ...")

    for (i <- 0 until d3Nodes.length) {
      if (d3Nodes(i).id == node.id) {
        d3Nodes(i).nodeText = node.nodeText
        d3Nodes(i).name = node.name
        d3Nodes(i).statusClass = node.statusClass
        d3Nodes(i).shape = node.shape
        d3Nodes(i).tooltip = node.tooltip
        d3Nodes(i).value = node.value
      }
    }

    if (d3Nodes.isEmpty || d3Nodes.filter(_.id == node.id).length == 0)
      d3Nodes = d3Nodes :+ node

    //    println(s"number of nodes after addNode ${d3Nodes.length}")
    if (needUpdate) update()
  }

  def removeNode(id: String): Unit = {
    println("remove node ...")

    print(s"nb node before ${d3Nodes.length}")
    d3Nodes = d3Nodes.filterNot(_.id == id)
    d3Links = d3Links.filterNot(link => link.source.id == id || link.target.id == id)

    update()
  }

  def removeAllLinks(needUpdate: Boolean = true): Unit = {
    println("remove all links ...")
    d3Links = js.Array[LinkD3]()
    if (needUpdate) update()
  }

  def removeAllNodes(needUpdate: Boolean = true): Unit = {
    println("remove all nodes ...")
    d3Nodes = js.Array[NodeD3]()
    if (needUpdate) update()
  }

  def addLink(link: LinkD3Json, needUpdate: Boolean = true): Unit = {
    println(s"addLink($link, $needUpdate)")
    for (i <- 0 until d3Links.size) {
      if (d3Links(i).source.id == link.source && d3Links(i).target.id == link.target) {
        d3Links(i).color = link.color
        d3Links(i).endArrow = link.endArrow
      }
    }

    val sourceOpt = d3Nodes.find(_.id == link.source)
    val targetOpt = d3Nodes.find(_.id == link.target)

    (sourceOpt, targetOpt) match {
      case (Some(source), Some(target)) => d3Links = d3Links :+ LinkD3(source, target, link.color, link.endArrow)
      case (None, Some(_)) => System.err.println(s"addLink($link): source ${link.source} not found in the nodes")
      case (Some(_), None) => System.err.println(s"addLink($link): target ${link.target} not found in the nodes")
      case _ => System.err.println(s"addLink($link): Neither source ${link.source} and target ${link.target} has been found in the nodes")
    }

    if (needUpdate) update()
  }


  private def parseJson(json: String): D3GraphData = {
    val graph = decode[D3GraphData](json)
    graph match {
      case Right(g) => g
      case Left(error) =>
        println(error)
        D3GraphData(List.empty[NodeD3], List.empty[LinkD3Json])
    }
  }

  private def filterLinks(allLinks: js.Array[LinkD3], curNodes: js.Array[NodeD3]) = {
    allLinks.filter(l => curNodes.contains(l.source) && curNodes.contains(l.target))
  }

  def update(): Unit = {


    val link = updateLink()

    val nodeCircle = updateNode(Circle, appendCircle, updateCirce)
    val nodeRect = updateNode(Rect, appendRect, updateRect)


    def forceTick(e: Event) = {
      link
        .attr("x1", (d: LinkD3) => d.source.x)
        .attr("y1", (d: LinkD3) => d.source.y)
        .attr("x2", (d: LinkD3) => d.target.x)
        .attr("y2", (d: LinkD3) => d.target.y)

      nodeCircle.attr("transform", (d: NodeD3) => "translate(" + d.x + "," + d.y + ")")
      nodeRect.attr("transform", (d: NodeD3) => "translate(" + d.x + "," + d.y + ")")


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

    val link = linkG.selectAll(".linkContainer")
      .data(d3Links, (d: LinkD3) => s"#${d.source.id}_#${d.target.id}")

    println(s"number of links in the updatesLinks ${d3Links.length}")

    val defs = refsG.selectAll(".defsContainer")
      .data(d3Links, (d: LinkD3) => s"#${d.source.id}_#${d.target.id}")

    val defsEnter = defs.enter().append("g")
      .attr("class", "defsContainer")

    defsEnter.append("marker")
      .attr("id", (t: LinkD3) => markerID(t.source,t.target))
      .attr("class", "markerClass")
      .attr("markerUnits", "userSpaceOnUse")
      .attr("markerWidth", "12")
      .attr("markerHeight", "18")
      .attr("refY", "6")
      .attr("orient", "auto")
      .append("path")
      .attr("d", "M 0 0 12 6 0 12 3 6")
      .attr("style", (t: LinkD3) => s"fill: ${t.color}")

    val linkEnter = link.enter().append("g")
      .attr("class", "linkContainer")

    linkEnter.append("line")
      .attr("class", "link")
      .attr("stroke", (d: LinkD3) => d.color)
      .attr("stroke-opacity", 0.8)
      .attr("x1", (d: LinkD3) => d.source.x)
      .attr("y1", (d: LinkD3) => d.source.y)
      .attr("x2", (d: LinkD3) => d.target.x)
      .attr("y2", (d: LinkD3) => d.target.y)
      .attr("marker-end", (d: LinkD3) => if (d.endArrow) s"url(#${markerID(d.source, d.target)})" else "")

    // Update
    defs.select("marker")
      .attr("refX", (t: LinkD3) => diameterRamp(t.target.value) + 12)
    defs.select("path")
      .attr("style", (t: LinkD3) => s"fill: ${t.color}")

    val line = link.select("line")
      .attr("stroke", (d: LinkD3) => d.color)
      .attr("marker-end", (d: LinkD3) => if (d.endArrow) s"url(#${markerID(d.source, d.target)}" else "")


    defs.exit().remove()
    link.exit().remove()
    line
  }

  private def updateNode(shape: Shape, nodeEnterSVGShape: (Shape, Selection[NodeD3]) => _, updateShape: (Shape, Update[NodeD3]) => _) = {
    val node = nodeG.selectAll(s".${shape.className}")
      .data(d3Nodes.filter(_.shape.name == shape.name), (d: NodeD3, _: Int) => d.id)

    println(s"number of nodes in the updatesNodes ${d3Nodes.length}")
    val nodeEnter = node.enter().append("g")
      .attr("class", shape.className)
      .call(force.drag)

    //ENTER
    nodeEnterSVGShape(shape, nodeEnter)

    // UPDATE
    updateShape(shape, node)

    // EXIT
    node.exit().remove()

    appendText(nodeEnter, node)
  }

  private def appendCircle(shape: Shape, s: Selection[NodeD3]) = {
    s.append(s"svg:${shape.name}")
      .style("stroke-width", 2.0)
      .attr("cx", (d: NodeD3) => d.x)
      .attr("cy", (d: NodeD3) => d.y)
  }

  private def updateCirce(shape: Shape, u: Update[NodeD3]) = {
    u.select(shape.name)
      .attr("r", (d: NodeD3) => diameterRamp(d.value))
      .attr("class", (d: NodeD3) => d.statusClass)
  }

  private def appendRect(shape: Shape, s: Selection[NodeD3]) = {
    s.append(s"svg:${shape.name}")
      .style("stroke-width", 2.0)
      .attr("x", (d: NodeD3) => -squareRamp(d.value) / 2)
      .attr("y", (d: NodeD3) => -squareRamp(d.value) / 2)
      .attr("width", (d: NodeD3) => squareRamp(d.value))
      .attr("height", (d: NodeD3) => squareRamp(d.value))
  }

  private def updateRect(shape: Shape, u: Update[NodeD3]) = {
    u.select(shape.name)
      .attr("class", (d: NodeD3) => d.statusClass)
  }

  private def appendText(nodeEnter: d3js.Selection[NodeD3], node: Update[NodeD3]): Update[NodeD3] = {
    nodeEnter.append("svg:text")
      .attr("class", "textClass")
      .attr("text-anchor", "right")

    node.select("text")
      //.attr("x", (d: NodeD3) => d.nodeText.length*2)
      .attr("dy", ".35em") // set offset y position
      .attr("dx", "1em") // set offset x position
      .text((d: NodeD3) => d.nodeText)

    node.on("click", (_: NodeD3) => tooltip.hideTooltip()).call(force.drag)
    node.on("mouseover", showDetails)
      .on("mouseout", hideDetails)
  }

  private def convertData(fromJson: D3GraphData): (js.Array[NodeD3], js.Array[LinkD3]) = {
    val nodes: js.Array[NodeD3] = fromJson.nodes.toJSArray
    println(s"nodes: $nodes")
    val nodesById = nodes.groupBy(_.id)
    println(s"nodes by ID: $nodesById")
    val links: js.Array[LinkD3] = fromJson.links.foldLeft(js.Array[LinkD3]())((array, node) => {
      val source = nodesById(node.source).head
      val target = nodesById(node.target).head
      array :+ LinkD3(source, target, node.color)
    })
    (nodes, links)
  }

  private def showDetails(node: NodeD3): Unit = {
    tooltip.showTooltip(node.tooltip, d3.event.asInstanceOf[dom.Event])
  }

  private def hideDetails(node: NodeD3): Unit = {
    tooltip.hideTooltip()
  }

  private def markerID(source: NodeD3, target: NodeD3) ={
    s"marker-${clean(source.id)}_${clean(target.id)}"
  }

  private def clean(url:String) = url.replaceAll(" ","")

}

/*
   Class representing data from JSON
  */
case class D3GraphData(nodes: List[NodeD3], links: List[LinkD3Json])

/*
   D3 specifics Class
  */
case class LinkD3(source: NodeD3, target: NodeD3, var color: String = "#000", var endArrow: Boolean = false) extends org.singlespaced.d3js.Link[NodeD3]

case class LinkD3Json(source: String, target: String, color: String = "#000", endArrow: Boolean = false)

case class NodeD3(id: String, var name: String, var value: Int, var tooltip: String = "", var nodeText: String = "node", var statusClass: String = "online", var shape: Shape = Circle) extends Node
