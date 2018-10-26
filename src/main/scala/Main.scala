import io.jorand.scalajs.Tooltip
import io.jorand.scalajs.d3graph.{D3Graph, LinkD3, LinkD3Json, NodeD3}

import scala.scalajs.js
import scala.util.Random

object Main {

  def setupGraph(): Unit = {
    D3Graph("svgDiv", 1000, 900, Tooltip("svg-tooltip", "auto"))
  }

  def main(args: Array[String]): Unit = {
    // Define a SVG canvas to host the graph
    val graph = D3Graph("svgDiv", 1000, 900, Tooltip("svg-tooltip", "auto"))

    graph.addNode(NodeD3("root","Root",10,nodeText = "JVM"))
    graph.addNode(NodeD3("scala","Scala",7, nodeText = "Scala"))
    graph.addNode(NodeD3("java","Java",1, nodeText = "Java"))
    graph.addLink(LinkD3Json("scala", "root"))
    graph.addLink(LinkD3Json("java", "root"))

//    // Create an iterator with a delay growing by increments on each iteration
//    val initialTimer = 3000
//    val increments = 2000
//    val it = (0 to 20).map(initialTimer + _ * increments).toIterator
//
//    // Create and show the initial graph
//    createGrape("",23, _ => "online", _ => "grey")
//
//    // Create the same grape but with different status and set the link color to random color, should update the node and the link
//    js.timers.setTimeout(it.next()) {
//      createGrape("", 30, i => if (i % 2 == 0) "online" else "offline", randomColor)
//    }
//
//    js.timers.setTimeout(it.next()) {
//      for (i <- 1 to 4) {
//        for (j <- 1 to 3) {
//          graph.addNode(NodeD3(s"new-$i-$j", s"the new Node $i$j", i, s"<p>tooltip $i;$j</p>", s"node-$i-$j", if (i % 2 == 0) "online" else "offline", shape(i)))
//          graph.addLink(LinkD3Json(s"new-$i", s"new-$i-$j", randomColor(j), width(j), endArrow = true))
//        }
//      }
//    }
//
//    // Create a new grape with another name
//    js.timers.setTimeout(it.next()) {
//      createGrape(",1", 45, i => if (i % 3 == 0) "online" else "offline")
//    }
//
//    // Link the two root
//    js.timers.setTimeout(it.next()) {
//      graph.addLink(LinkD3Json("newroot,1", "newroot", "#00e"))
//    }
//
//    // Remove some nodes
//    js.timers.setTimeout(it.next()) {
//      graph.removeNode("new-1")
//      graph.removeNode("new-2")
//      graph.removeNode("new-3")
//      graph.removeNode("new-4")
//      graph.removeNode("new-5")
//    }
//
//    // Remove all links
//    js.timers.setTimeout(it.next()) {
//      graph.removeAllLinks()
//    }
//
//    js.timers.setTimeout(it.next()) {
//      graph.markNode("rootNode")(d => true)
//      graph.markNode("offline")(d => d.id contains "newroot")
//      graph.update()
//    }
//
//    // Remove all the nodes
//    js.timers.setTimeout(it.next()) {
//      graph.removeAllNodes()
//    }
//
//    // Recreate a grape to inspect the structure in the browser.
//    js.timers.setTimeout(it.next()) {
//      createGrape("--", 14, i => if (i % 4 == 0) "online" else "offline")
//    }
//
//    js.timers.setTimeout(it.next()) {
//      for (i <- 1 to 5) {
//        for (j <- 1 to 4) {
//          graph.addNode(NodeD3(s"new-$i-$j", s"the new Node $i$j", i, s"<p>tooltip $i;$j</p>", s"node-$i-$j", if (i % 2 == 0) "online" else "offline", shape(i)))
//          graph.addLink(LinkD3Json(s"new-$i-$j", s"new-$i--", randomColor(j), j, endArrow = true))
//        }
//      }
//    }

    def randomColor(i: Int) = {
      val arrayColor = Array("#a6a6a6", "#9ff44d", "#ff84ff")
      arrayColor(Random.nextInt(arrayColor.length))
    }



    def shape(i: Int): D3Graph.Shape = i match {
      case t if t % 3 == 0 => D3Graph.Rect
      case _ => D3Graph.Circle
    }

    def color(i: Int): String = i match {
      case t if t % 3 == 0 => "#f00"
      case _ => "#222"
    }

    def width(i: Int): Float = i match {
      case t if t % 3 == 0 => 0.5f
      case t if t % 3 == 1 => 1f
      case _ => 3f
    }


    def createGrape(prefix: String, rootValue: Int, status: Int => String, linkColor: Int => String = i => color(i), value: Int => Int = i => i): Unit = {
      graph.addNode(NodeD3(s"newroot$prefix", "the new Node", rootValue, "<p>tooltip</p>", "root", "rootNode"))
      for (i <- 1 to 10) {
        graph.addNode(NodeD3(s"new-$i$prefix", s"the new Node $i$prefix", value(i), s"<p>tooltip $i$prefix</p>", s"node-$i$prefix", status(i), shape(i)))
        graph.addLink(LinkD3Json(s"new-$i$prefix", s"newroot$prefix", linkColor(i), width(i), endArrow = true))
      }
    }

  }
}
